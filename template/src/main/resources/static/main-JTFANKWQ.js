var k=Object.create;var g=Object.defineProperty;var l$1=Object.defineProperties;var m=Object.getOwnPropertyDescriptor;var n=Object.getOwnPropertyDescriptors;var o=Object.getOwnPropertyNames;var f$1=Object.getOwnPropertySymbols;var p$1=Object.getPrototypeOf;var h$1=Object.prototype.hasOwnProperty;var j=Object.prototype.propertyIsEnumerable;var i=(a,b,c)=>b in a?g(a,b,{enumerable:!0,configurable:!0,writable:!0,value:c}):a[b]=c;var r=(a,b)=>{for(var c in b||={})h$1.call(b,c)&&i(a,c,b[c]);if(f$1)for(var c of f$1(b))j.call(b,c)&&i(a,c,b[c]);return a};var s=(a,b)=>l$1(a,n(b));var t=(a,b)=>{var c={};for(var d in a)h$1.call(a,d)&&b.indexOf(d)<0&&(c[d]=a[d]);if(a!=null&&f$1)for(var d of f$1(a))b.indexOf(d)<0&&j.call(a,d)&&(c[d]=a[d]);return c};var u=(a,b)=>()=>{try{return b||a((b={exports:{}}).exports,b),b.exports}catch(c){throw b=0,c}};var q$1=(a,b,c,d)=>{if(b&&typeof b==`object`||typeof b==`function`)for(let e of o(b))!h$1.call(a,e)&&e!==c&&g(a,e,{get:()=>b[e],enumerable:!(d=m(b,e))||d.enumerable});return a};var v=(a,b,c)=>(c=a!=null?k(p$1(a)):{},q$1(b||!a||!a.__esModule?g(c,`default`,{value:a,enumerable:!0}):c,a));var Rw=u((pm,Ow)=>{"use strict";(function(t,n){typeof pm==`object`&&typeof Ow<`u`?n(pm):typeof define==`function`&&define.amd?define([`exports`],n):(t=typeof globalThis<`u`?globalThis:t||self,n(t.StompJs={}))})(pm,(function(t){"use strict";function n(j,x){j.terminate=function(){let k=()=>{};this.onerror=k,this.onmessage=k,this.onopen=k;let I=new Date,U=Math.random().toString().substring(2,8),ge=this.onclose;this.onclose=ht=>{let Ge=new Date().getTime()-I.getTime();x(`Discarded socket (#${U})  closed after ${Ge}ms, with code/reason: ${ht.code}/${ht.reason}`)},this.close(),ge?.call(j,{code:4001,reason:`Quick discarding socket (#${U}) without waiting for the shutdown sequence.`,wasClean:!1})}}let e={LF:`
`,NULL:`\0`};class i{get body(){return!this._body&&this.isBinaryBody&&(this._body=new TextDecoder().decode(this._binaryBody)),this._body||``}get binaryBody(){return!this._binaryBody&&!this.isBinaryBody&&(this._binaryBody=new TextEncoder().encode(this._body)),this._binaryBody}constructor(x){let{command:k,headers:I,body:U,binaryBody:ge,escapeHeaderValues:ht,skipContentLengthHeader:Ge}=x;this.command=k,this.headers=Object.assign({},I||{}),ge?(this._binaryBody=ge,this.isBinaryBody=!0):(this._body=U||``,this.isBinaryBody=!1),this.escapeHeaderValues=ht||!1,this.skipContentLengthHeader=Ge||!1}static fromRawFrame(x,k){let I={},U=ge=>ge.replace(/^\s+|\s+$/g,``);for(let ge of x.headers.reverse()){ge.indexOf(`:`);let ht=U(ge[0]),Ge=U(ge[1]);k&&x.command!==`CONNECT`&&x.command!==`CONNECTED`&&(Ge=i.hdrValueUnEscape(Ge)),I[ht]=Ge}return new i({command:x.command,headers:I,binaryBody:x.binaryBody,escapeHeaderValues:k})}toString(){return this.serializeCmdAndHeaders()}serialize(){let x=this.serializeCmdAndHeaders();return this.isBinaryBody?i.toUnit8Array(x,this._binaryBody).buffer:x+this._body+e.NULL}serializeCmdAndHeaders(){let x=[this.command];this.skipContentLengthHeader&&delete this.headers[`content-length`];for(let k of Object.keys(this.headers||{})){let I=this.headers[k];this.escapeHeaderValues&&this.command!==`CONNECT`&&this.command!==`CONNECTED`?x.push(`${k}:${i.hdrValueEscape(`${I}`)}`):x.push(`${k}:${I}`)}return(this.isBinaryBody||!this.isBodyEmpty()&&!this.skipContentLengthHeader)&&x.push(`content-length:${this.bodyLength()}`),x.join(e.LF)+e.LF+e.LF}isBodyEmpty(){return this.bodyLength()===0}bodyLength(){let x=this.binaryBody;return x?x.length:0}static sizeOfUTF8(x){return x?new TextEncoder().encode(x).length:0}static toUnit8Array(x,k){let I=new TextEncoder().encode(x),U=new Uint8Array([0]),ge=new Uint8Array(I.length+k.length+U.length);return ge.set(I),ge.set(k,I.length),ge.set(U,I.length+k.length),ge}static marshall(x){return new i(x).serialize()}static hdrValueEscape(x){return x.replace(/\\/g,`\\\\`).replace(/\r/g,`\\r`).replace(/\n/g,`\\n`).replace(/:/g,`\\c`)}static hdrValueUnEscape(x){return x.replace(/\\r/g,`\r`).replace(/\\n/g,`
`).replace(/\\c/g,`:`).replace(/\\\\/g,`\\`)}}let a=0,o=10,r=13,s=58;class c{constructor(x,k){this.onFrame=x,this.onIncomingPing=k,this._encoder=new TextEncoder,this._decoder=new TextDecoder,this._token=[],this._initState()}parseChunk(x,k=!1){let I;if(typeof x==`string`?I=this._encoder.encode(x):I=new Uint8Array(x),k&&I[I.length-1]!==0){let U=new Uint8Array(I.length+1);U.set(I,0),U[I.length]=0,I=U}for(let U=0;U<I.length;U++){let ge=I[U];this._onByte(ge)}}_collectFrame(x){if(x!==a&&x!==r){if(x===o){this.onIncomingPing();return}this._onByte=this._collectCommand,this._reinjectByte(x)}}_collectCommand(x){if(x!==r){if(x===o){this._results.command=this._consumeTokenAsUTF8(),this._onByte=this._collectHeaders;return}this._consumeByte(x)}}_collectHeaders(x){if(x!==r){if(x===o){this._setupCollectBody();return}this._onByte=this._collectHeaderKey,this._reinjectByte(x)}}_reinjectByte(x){this._onByte(x)}_collectHeaderKey(x){if(x===s){this._headerKey=this._consumeTokenAsUTF8(),this._onByte=this._collectHeaderValue;return}this._consumeByte(x)}_collectHeaderValue(x){if(x!==r){if(x===o){this._results.headers.push([this._headerKey,this._consumeTokenAsUTF8()]),this._headerKey=void 0,this._onByte=this._collectHeaders;return}this._consumeByte(x)}}_setupCollectBody(){let x=this._results.headers.filter(k=>k[0]===`content-length`)[0];x?(this._bodyBytesRemaining=parseInt(x[1],10),this._onByte=this._collectBodyFixedSize):this._onByte=this._collectBodyNullTerminated}_collectBodyNullTerminated(x){if(x===a){this._retrievedBody();return}this._consumeByte(x)}_collectBodyFixedSize(x){if(this._bodyBytesRemaining--===0){this._retrievedBody();return}this._consumeByte(x)}_retrievedBody(){this._results.binaryBody=this._consumeTokenAsRaw();try{this.onFrame(this._results)}catch(x){console.log(`Ignoring an exception thrown by a frame handler. Original exception: `,x)}this._initState()}_consumeByte(x){this._token.push(x)}_consumeTokenAsUTF8(){return this._decoder.decode(this._consumeTokenAsRaw())}_consumeTokenAsRaw(){let x=new Uint8Array(this._token);return this._token=[],x}_initState(){this._results={command:void 0,headers:[],binaryBody:void 0},this._token=[],this._headerKey=void 0,this._onByte=this._collectFrame}}t.StompSocketState=void 0,(function(j){j[j.CONNECTING=0]=`CONNECTING`,j[j.OPEN=1]=`OPEN`,j[j.CLOSING=2]=`CLOSING`,j[j.CLOSED=3]=`CLOSED`})(t.StompSocketState||(t.StompSocketState={})),t.ActivationState=void 0,(function(j){j[j.ACTIVE=0]=`ACTIVE`,j[j.DEACTIVATING=1]=`DEACTIVATING`,j[j.INACTIVE=2]=`INACTIVE`})(t.ActivationState||(t.ActivationState={})),t.ReconnectionTimeMode=void 0,(function(j){j[j.LINEAR=0]=`LINEAR`,j[j.EXPONENTIAL=1]=`EXPONENTIAL`})(t.ReconnectionTimeMode||(t.ReconnectionTimeMode={})),t.TickerStrategy=void 0,(function(j){j.Interval=`interval`,j.Worker=`worker`})(t.TickerStrategy||(t.TickerStrategy={}));class m{constructor(x,k=t.TickerStrategy.Interval,I){this._interval=x,this._strategy=k,this._debug=I,this._workerScript=`
    var startTime = Date.now();
    setInterval(function() {
        self.postMessage(Date.now() - startTime);
    }, ${this._interval});
  `}start(x){this.stop(),this.shouldUseWorker()?this.runWorker(x):this.runInterval(x)}stop(){this.disposeWorker(),this.disposeInterval()}shouldUseWorker(){return typeof Worker<`u`&&this._strategy===t.TickerStrategy.Worker}runWorker(x){this._debug(`Using runWorker for outgoing pings`),this._worker||(this._worker=new Worker(URL.createObjectURL(new Blob([this._workerScript],{type:`text/javascript`}))),this._worker.onmessage=k=>x(k.data))}runInterval(x){if(this._debug(`Using runInterval for outgoing pings`),!this._timer){let k=Date.now();this._timer=setInterval(()=>{x(Date.now()-k)},this._interval)}}disposeWorker(){this._worker&&(this._worker.terminate(),delete this._worker,this._debug(`Outgoing ping disposeWorker`))}disposeInterval(){this._timer&&(clearInterval(this._timer),delete this._timer,this._debug(`Outgoing ping disposeInterval`))}}class u{constructor(x){this.versions=x}supportedVersions(){return this.versions.join(`,`)}protocolVersions(){return this.versions.map(x=>`v${x.replace(`.`,``)}.stomp`)}}u.V1_0=`1.0`,u.V1_1=`1.1`,u.V1_2=`1.2`,u.default=new u([u.V1_2,u.V1_1,u.V1_0]);class g{get connectedVersion(){return this._connectedVersion}get connected(){return this._connected}constructor(x,k,I){this._client=x,this._webSocket=k,this._connected=!1,this._serverFrameHandlers={CONNECTED:U=>{this.debug(`connected to server ${U.headers.server}`),this._connected=!0,this._connectedVersion=U.headers.version,this._connectedVersion===u.V1_2&&(this._escapeHeaderValues=!0),this._setupHeartbeat(U.headers),this.onConnect(U)},MESSAGE:U=>{let ge=U.headers.subscription,ht=this._subscriptions[ge]||this.onUnhandledMessage,Ge=U,le=this,ke=this._connectedVersion===u.V1_2?Ge.headers.ack:Ge.headers[`message-id`];Ge.ack=(F={})=>le.ack(ke,ge,F),Ge.nack=(F={})=>le.nack(ke,ge,F),ht(Ge)},RECEIPT:U=>{let ge=this._receiptWatchers[U.headers[`receipt-id`]];ge?(ge(U),delete this._receiptWatchers[U.headers[`receipt-id`]]):this.onUnhandledReceipt(U)},ERROR:U=>{this.onStompError(U)}},this._counter=0,this._subscriptions={},this._receiptWatchers={},this._partialData=``,this._escapeHeaderValues=!1,this._lastServerActivityTS=Date.now(),this.debug=I.debug,this.stompVersions=I.stompVersions,this.connectHeaders=I.connectHeaders,this.disconnectHeaders=I.disconnectHeaders,this.heartbeatIncoming=I.heartbeatIncoming,this.heartbeatToleranceMultiplier=I.heartbeatGracePeriods,this.heartbeatOutgoing=I.heartbeatOutgoing,this.splitLargeFrames=I.splitLargeFrames,this.maxWebSocketChunkSize=I.maxWebSocketChunkSize,this.forceBinaryWSFrames=I.forceBinaryWSFrames,this.logRawCommunication=I.logRawCommunication,this.appendMissingNULLonIncoming=I.appendMissingNULLonIncoming,this.discardWebsocketOnCommFailure=I.discardWebsocketOnCommFailure,this.onConnect=I.onConnect,this.onDisconnect=I.onDisconnect,this.onStompError=I.onStompError,this.onWebSocketClose=I.onWebSocketClose,this.onWebSocketError=I.onWebSocketError,this.onUnhandledMessage=I.onUnhandledMessage,this.onUnhandledReceipt=I.onUnhandledReceipt,this.onUnhandledFrame=I.onUnhandledFrame,this.onHeartbeatReceived=I.onHeartbeatReceived,this.onHeartbeatLost=I.onHeartbeatLost}start(){let x=new c(I=>{let U=i.fromRawFrame(I,this._escapeHeaderValues);this.logRawCommunication||this.debug(`<<< ${U}`),(this._serverFrameHandlers[U.command]||this.onUnhandledFrame)(U)},()=>{this.debug(`<<< PONG`),this.onHeartbeatReceived()});this._webSocket.onmessage=I=>{if(this.debug(`Received data`),this._lastServerActivityTS=Date.now(),this.logRawCommunication){let U=I.data instanceof ArrayBuffer?new TextDecoder().decode(I.data):I.data;this.debug(`<<< ${U}`)}x.parseChunk(I.data,this.appendMissingNULLonIncoming)},this._webSocket.onclose=I=>{this.debug(`Connection closed to ${this._webSocket.url}`),this._cleanUp(),this.onWebSocketClose(I)},this._webSocket.onerror=I=>{this.onWebSocketError(I)};let k=()=>{let I=Object.assign({},this.connectHeaders);this.debug(`Web Socket Opened...`),I[`accept-version`]=this.stompVersions.supportedVersions(),I[`heart-beat`]=[this.heartbeatOutgoing,this.heartbeatIncoming].join(`,`),this._transmit({command:`CONNECT`,headers:I})};this._webSocket.readyState===t.StompSocketState.OPEN?k():this._webSocket.onopen=k}_setupHeartbeat(x){if(x.version!==u.V1_1&&x.version!==u.V1_2||!x[`heart-beat`])return;let[k,I]=x[`heart-beat`].split(`,`).map(U=>parseInt(U,10));if(this.heartbeatOutgoing!==0&&I!==0){let U=Math.max(this.heartbeatOutgoing,I);this.debug(`send PING every ${U}ms`),this._pinger=new m(U,this._client.heartbeatStrategy,this.debug),this._pinger.start(()=>{this._webSocket.readyState===t.StompSocketState.OPEN&&(this._webSocket.send(e.LF),this.debug(`>>> PING`))})}if(this.heartbeatIncoming!==0&&k!==0){let U=Math.max(this.heartbeatIncoming,k);this.debug(`check PONG every ${U}ms`),this._ponger=setInterval(()=>{let ge=Date.now()-this._lastServerActivityTS;ge>U*this.heartbeatToleranceMultiplier&&(this.debug(`did not receive server activity for the last ${ge}ms`),this.onHeartbeatLost(),this._closeOrDiscardWebsocket())},U)}}_closeOrDiscardWebsocket(){this.discardWebsocketOnCommFailure?(this.debug(`Discarding websocket, the underlying socket may linger for a while`),this.discardWebsocket()):(this.debug(`Issuing close on the websocket`),this._closeWebsocket())}forceDisconnect(){this._webSocket&&(this._webSocket.readyState===t.StompSocketState.CONNECTING||this._webSocket.readyState===t.StompSocketState.OPEN)&&this._closeOrDiscardWebsocket()}_closeWebsocket(){this._webSocket.onmessage=()=>{},this._webSocket.close()}discardWebsocket(){typeof this._webSocket.terminate!=`function`&&n(this._webSocket,x=>this.debug(x)),this._webSocket.terminate()}_transmit(x){let{command:k,headers:I,body:U,binaryBody:ge,skipContentLengthHeader:ht}=x,Ge=new i({command:k,headers:I,body:U,binaryBody:ge,escapeHeaderValues:this._escapeHeaderValues,skipContentLengthHeader:ht}),le=Ge.serialize();if(this.logRawCommunication?this.debug(`>>> ${le}`):this.debug(`>>> ${Ge}`),this.forceBinaryWSFrames&&typeof le==`string`&&(le=new TextEncoder().encode(le)),typeof le!=`string`||!this.splitLargeFrames)this._webSocket.send(le);else{let ke=le;for(;ke.length>0;){let F=ke.substring(0,this.maxWebSocketChunkSize);ke=ke.substring(this.maxWebSocketChunkSize),this._webSocket.send(F),this.debug(`chunk sent = ${F.length}, remaining = ${ke.length}`)}}}dispose(){if(this.connected)try{let x=Object.assign({},this.disconnectHeaders);x.receipt||(x.receipt=`close-${this._counter++}`),this.watchForReceipt(x.receipt,k=>{this._closeWebsocket(),this._cleanUp(),this.onDisconnect(k)}),this._transmit({command:`DISCONNECT`,headers:x})}catch(x){this.debug(`Ignoring error during disconnect ${x}`)}else(this._webSocket.readyState===t.StompSocketState.CONNECTING||this._webSocket.readyState===t.StompSocketState.OPEN)&&this._closeWebsocket()}_cleanUp(){this._connected=!1,this._pinger&&(this._pinger.stop(),this._pinger=void 0),this._ponger&&(clearInterval(this._ponger),this._ponger=void 0)}publish(x){let{destination:k,headers:I,body:U,binaryBody:ge,skipContentLengthHeader:ht}=x,Ge=Object.assign({destination:k},I);this._transmit({command:`SEND`,headers:Ge,body:U,binaryBody:ge,skipContentLengthHeader:ht})}watchForReceipt(x,k){this._receiptWatchers[x]=k}subscribe(x,k,I={}){I=Object.assign({},I),I.id||(I.id=`sub-${this._counter++}`),I.destination=x,this._subscriptions[I.id]=k,this._transmit({command:`SUBSCRIBE`,headers:I});let U=this;return{id:I.id,unsubscribe(ge){return U.unsubscribe(I.id,ge)}}}unsubscribe(x,k={}){k=Object.assign({},k),delete this._subscriptions[x],k.id=x,this._transmit({command:`UNSUBSCRIBE`,headers:k})}begin(x){let k=x||`tx-${this._counter++}`;this._transmit({command:`BEGIN`,headers:{transaction:k}});let I=this;return{id:k,commit(){I.commit(k)},abort(){I.abort(k)}}}commit(x){this._transmit({command:`COMMIT`,headers:{transaction:x}})}abort(x){this._transmit({command:`ABORT`,headers:{transaction:x}})}ack(x,k,I={}){I=Object.assign({},I),this._connectedVersion===u.V1_2?I.id=x:I[`message-id`]=x,I.subscription=k,this._transmit({command:`ACK`,headers:I})}nack(x,k,I={}){return I=Object.assign({},I),this._connectedVersion===u.V1_2?I.id=x:I[`message-id`]=x,I.subscription=k,this._transmit({command:`NACK`,headers:I})}}class v{get webSocket(){return this._stompHandler?._webSocket}get disconnectHeaders(){return this._disconnectHeaders}set disconnectHeaders(x){this._disconnectHeaders=x,this._stompHandler&&(this._stompHandler.disconnectHeaders=this._disconnectHeaders)}get connected(){return!!this._stompHandler&&this._stompHandler.connected}get connectedVersion(){return this._stompHandler?this._stompHandler.connectedVersion:void 0}get active(){return this.state===t.ActivationState.ACTIVE}_changeState(x){this.state=x,this.onChangeState(x)}constructor(x={}){this.stompVersions=u.default,this.connectionTimeout=0,this.reconnectDelay=5e3,this._nextReconnectDelay=0,this.maxReconnectDelay=9e5,this.reconnectTimeMode=t.ReconnectionTimeMode.LINEAR,this.heartbeatIncoming=1e4,this.heartbeatToleranceMultiplier=2,this.heartbeatOutgoing=1e4,this.heartbeatStrategy=t.TickerStrategy.Interval,this.splitLargeFrames=!1,this.maxWebSocketChunkSize=8192,this.forceBinaryWSFrames=!1,this.appendMissingNULLonIncoming=!1,this.discardWebsocketOnCommFailure=!1,this.state=t.ActivationState.INACTIVE;let k=()=>{};this.debug=k,this.beforeConnect=k,this.onConnect=k,this.onDisconnect=k,this.onUnhandledMessage=k,this.onUnhandledReceipt=k,this.onUnhandledFrame=k,this.onHeartbeatReceived=k,this.onHeartbeatLost=k,this.onStompError=k,this.onWebSocketClose=k,this.onWebSocketError=k,this.logRawCommunication=!1,this.onChangeState=k,this.connectHeaders={},this._disconnectHeaders={},this.configure(x)}configure(x){Object.assign(this,x),this.maxReconnectDelay>0&&this.maxReconnectDelay<this.reconnectDelay&&(this.debug(`Warning: maxReconnectDelay (${this.maxReconnectDelay}ms) is less than reconnectDelay (${this.reconnectDelay}ms). Using reconnectDelay as the maxReconnectDelay delay.`),this.maxReconnectDelay=this.reconnectDelay)}activate(){let x=()=>{if(this.active){this.debug(`Already ACTIVE, ignoring request to activate`);return}this._changeState(t.ActivationState.ACTIVE),this._nextReconnectDelay=this.reconnectDelay,this._connect()};this.state===t.ActivationState.DEACTIVATING?(this.debug(`Waiting for deactivation to finish before activating`),this.deactivate().then(()=>{x()})):x()}async _connect(){if(await this.beforeConnect(this),this._stompHandler){this.debug(`There is already a stompHandler, skipping the call to connect`);return}if(!this.active){this.debug(`Client has been marked inactive, will not attempt to connect`);return}this.connectionTimeout>0&&(this._connectionWatcher&&clearTimeout(this._connectionWatcher),this._connectionWatcher=setTimeout(()=>{this.connected||(this.debug(`Connection not established in ${this.connectionTimeout}ms, closing socket`),this.forceDisconnect())},this.connectionTimeout)),this.debug(`Opening Web Socket...`);let x=this._createWebSocket();this._stompHandler=new g(this,x,{debug:this.debug,stompVersions:this.stompVersions,connectHeaders:this.connectHeaders,disconnectHeaders:this._disconnectHeaders,heartbeatIncoming:this.heartbeatIncoming,heartbeatGracePeriods:this.heartbeatToleranceMultiplier,heartbeatOutgoing:this.heartbeatOutgoing,heartbeatStrategy:this.heartbeatStrategy,splitLargeFrames:this.splitLargeFrames,maxWebSocketChunkSize:this.maxWebSocketChunkSize,forceBinaryWSFrames:this.forceBinaryWSFrames,logRawCommunication:this.logRawCommunication,appendMissingNULLonIncoming:this.appendMissingNULLonIncoming,discardWebsocketOnCommFailure:this.discardWebsocketOnCommFailure,onConnect:k=>{if(this._connectionWatcher&&(clearTimeout(this._connectionWatcher),this._connectionWatcher=void 0),this._nextReconnectDelay=this.reconnectDelay,!this.active){this.debug(`STOMP got connected while deactivate was issued, will disconnect now`),this._disposeStompHandler();return}this.onConnect(k)},onDisconnect:k=>{this.onDisconnect(k)},onStompError:k=>{this.onStompError(k)},onWebSocketClose:k=>{this._stompHandler=void 0,this.state===t.ActivationState.DEACTIVATING&&this._changeState(t.ActivationState.INACTIVE),this.onWebSocketClose(k),this.active&&this._schedule_reconnect()},onWebSocketError:k=>{this.onWebSocketError(k)},onUnhandledMessage:k=>{this.onUnhandledMessage(k)},onUnhandledReceipt:k=>{this.onUnhandledReceipt(k)},onUnhandledFrame:k=>{this.onUnhandledFrame(k)},onHeartbeatReceived:()=>{this.onHeartbeatReceived()},onHeartbeatLost:()=>{this.onHeartbeatLost()}}),this._stompHandler.start()}_createWebSocket(){let x;if(this.webSocketFactory)x=this.webSocketFactory();else if(this.brokerURL)x=new WebSocket(this.brokerURL,this.stompVersions.protocolVersions());else throw new Error(`Either brokerURL or webSocketFactory must be provided`);return x.binaryType=`arraybuffer`,x}_schedule_reconnect(){this._nextReconnectDelay>0&&(this.debug(`STOMP: scheduling reconnection in ${this._nextReconnectDelay}ms`),this._reconnector=setTimeout(()=>{this.reconnectTimeMode===t.ReconnectionTimeMode.EXPONENTIAL&&(this._nextReconnectDelay=this._nextReconnectDelay*2,this.maxReconnectDelay!==0&&(this._nextReconnectDelay=Math.min(this._nextReconnectDelay,this.maxReconnectDelay))),this._connect()},this._nextReconnectDelay))}async deactivate(x={}){let k=x.force||!1,I=this.active,U;if(this.state===t.ActivationState.INACTIVE)return this.debug(`Already INACTIVE, nothing more to do`),Promise.resolve();if(this._changeState(t.ActivationState.DEACTIVATING),this._nextReconnectDelay=0,this._reconnector&&(clearTimeout(this._reconnector),this._reconnector=void 0),this._stompHandler&&this.webSocket.readyState!==t.StompSocketState.CLOSED){let ge=this._stompHandler.onWebSocketClose;U=new Promise((ht,Ge)=>{this._stompHandler.onWebSocketClose=le=>{ge(le),ht()}})}else return this._changeState(t.ActivationState.INACTIVE),Promise.resolve();return k?this._stompHandler?.discardWebsocket():I&&this._disposeStompHandler(),U}forceDisconnect(){this._stompHandler&&this._stompHandler.forceDisconnect()}_disposeStompHandler(){this._stompHandler&&this._stompHandler.dispose()}publish(x){this._checkConnection(),this._stompHandler.publish(x)}_checkConnection(){if(!this.connected)throw new TypeError(`There is no underlying STOMP connection`)}watchForReceipt(x,k){this._checkConnection(),this._stompHandler.watchForReceipt(x,k)}subscribe(x,k,I={}){return this._checkConnection(),this._stompHandler.subscribe(x,k,I)}unsubscribe(x,k={}){this._checkConnection(),this._stompHandler.unsubscribe(x,k)}begin(x){return this._checkConnection(),this._stompHandler.begin(x)}commit(x){this._checkConnection(),this._stompHandler.commit(x)}abort(x){this._checkConnection(),this._stompHandler.abort(x)}ack(x,k,I={}){this._checkConnection(),this._stompHandler.ack(x,k,I)}nack(x,k,I={}){this._checkConnection(),this._stompHandler.nack(x,k,I)}}class b{}class y{}class E{constructor(x){this.client=x}get outgoing(){return this.client.heartbeatOutgoing}set outgoing(x){this.client.heartbeatOutgoing=x}get incoming(){return this.client.heartbeatIncoming}set incoming(x){this.client.heartbeatIncoming=x}}class R extends v{constructor(x){super(),this.maxWebSocketFrameSize=16384,this._heartbeatInfo=new E(this),this.reconnect_delay=0,this.webSocketFactory=x,this.debug=(...k)=>{console.log(...k)}}_parseConnect(...x){let k,I,U,ge={};if(x.length<2)throw new Error(`Connect requires at least 2 arguments`);return typeof x[1]==`function`?[ge,I,U,k]=x:x.length===6?[ge.login,ge.passcode,I,U,k,ge.host]=x:[ge.login,ge.passcode,I,U,k]=x,[ge,I,U,k]}connect(...x){let k=this._parseConnect(...x);k[0]&&(this.connectHeaders=k[0]),k[1]&&(this.onConnect=k[1]),k[2]&&(this.onStompError=k[2]),k[3]&&(this.onWebSocketClose=k[3]),super.activate()}disconnect(x,k={}){x&&(this.onDisconnect=x),this.disconnectHeaders=k,super.deactivate()}send(x,k={},I=``){k=Object.assign({},k);let U=k[`content-length`]===!1;U&&delete k[`content-length`],this.publish({destination:x,headers:k,body:I,skipContentLengthHeader:U})}set reconnect_delay(x){this.reconnectDelay=x}get ws(){return this.webSocket}get version(){return this.connectedVersion}get onreceive(){return this.onUnhandledMessage}set onreceive(x){this.onUnhandledMessage=x}get onreceipt(){return this.onUnhandledReceipt}set onreceipt(x){this.onUnhandledReceipt=x}get heartbeat(){return this._heartbeatInfo}set heartbeat(x){this.heartbeatIncoming=x.incoming,this.heartbeatOutgoing=x.outgoing}}class oe{static client(x,k){k??=u.default.protocolVersions();let I=()=>{return new(oe.WebSocketClass||WebSocket)(x,k)};return new R(I)}static over(x){let k;return typeof x==`function`?k=x:(console.warn(`Stomp.over did not receive a factory, auto reconnect will not work. Please see https://stomp-js.github.io/api-docs/latest/classes/Stomp.html#over`),k=()=>x),new R(k)}}oe.WebSocketClass=null,t.Client=v,t.CompatClient=R,t.FrameImpl=i,t.Parser=c,t.Stomp=oe,t.StompConfig=b,t.StompHeaders=y,t.Versions=u}))});var tn=null;var Xc=!1;var ja=1;var rE=null;var yt=Symbol(`SIGNAL`);function pe(t){let n=tn;return tn=t,n}function tl(){return tn}var Ha={version:0,lastCleanEpoch:0,dirty:!1,producers:void 0,producersTail:void 0,consumers:void 0,consumersTail:void 0,recomputing:!1,consumerAllowSignalWrites:!1,consumerIsAlwaysLive:!1,kind:`unknown`,producerMustRecompute:()=>!1,producerRecomputeValue:()=>{},consumerMarkedDirty:()=>{},consumerOnSignalRead:()=>{}};function Ai(t){if(Xc)throw new Error(``);if(tn===null)return;tn.consumerOnSignalRead(t);let n=tn.producersTail;if(n!==void 0&&n.producer===t)return;let e,i=tn.recomputing;if(i&&(e=n!==void 0?n.nextProducer:tn.producers,e!==void 0&&e.producer===t)){tn.producersTail=e,e.lastReadVersion=t.version,e.knownValidAtEpoch=ja;return}let a=t.consumersTail;if(a!==void 0&&a.consumer===tn&&(!i||a.knownValidAtEpoch===ja))return;let o=Ho(tn),r={producer:t,consumer:tn,nextProducer:e,prevConsumer:void 0,knownValidAtEpoch:ja,lastReadVersion:t.version,nextConsumer:void 0};tn.producersTail=r,n!==void 0?n.nextProducer=r:tn.producers=r,o&&jb(t,r)}function Lb(){ja++}function ds(t){if(!(Ho(t)&&!t.dirty)&&!(!t.dirty&&t.lastCleanEpoch===ja)){if(!t.producerMustRecompute(t)&&!zo(t)){el(t);return}t.producerRecomputeValue(t),el(t)}}function Vm(t){if(t.consumers===void 0)return;let n=Xc;Xc=!0;try{for(let e=t.consumers;e!==void 0;e=e.nextConsumer){let i=e.consumer;i.dirty||sE(i)}}finally{Xc=n}}function Bm(){return tn?.consumerAllowSignalWrites!==!1}function sE(t){t.dirty=!0,Vm(t),t.consumerMarkedDirty?.(t)}function el(t){t.dirty=!1,t.lastCleanEpoch=ja}function ca(t){return t&&Vb(t),pe(t)}function Vb(t){if(t.producersTail?.knownValidAtEpoch===ja){let n=t.producers;for(;n!==void 0;)n.knownValidAtEpoch=null,n=n.nextProducer}t.producersTail=void 0,t.recomputing=!0}function Ua(t,n){pe(n),t&&Bb(t)}function Bb(t){t.recomputing=!1;let n=t.producersTail,e=n!==void 0?n.nextProducer:t.producers;if(e!==void 0){if(Ho(t))do e=jm(e);while(e!==void 0);n!==void 0?n.nextProducer=void 0:t.producers=void 0}}function zo(t){for(let n=t.producers;n!==void 0;n=n.nextProducer){let e=n.producer,i=n.lastReadVersion;if(i!==e.version||(ds(e),i!==e.version))return!0}return!1}function la(t){if(Ho(t)){let n=t.producers;for(;n!==void 0;)n=jm(n)}t.producers=void 0,t.producersTail=void 0,t.consumers=void 0,t.consumersTail=void 0}function jb(t,n){let e=t.consumersTail,i=Ho(t);if(e!==void 0?(n.nextConsumer=e.nextConsumer,e.nextConsumer=n):(n.nextConsumer=void 0,t.consumers=n),n.prevConsumer=e,t.consumersTail=n,!i)for(let a=t.producers;a!==void 0;a=a.nextProducer)jb(a.producer,a)}function jm(t){let n=t.producer,e=t.nextProducer,i=t.nextConsumer,a=t.prevConsumer;if(t.nextConsumer=void 0,t.prevConsumer=void 0,i!==void 0?i.prevConsumer=a:n.consumersTail=a,a!==void 0)a.nextConsumer=i;else if(n.consumers=i,!Ho(n)){let o=n.producers;for(;o!==void 0;)o=jm(o)}return e}function Ho(t){return t.consumerIsAlwaysLive||t.consumers!==void 0}function nl(t){rE?.(t)}function il(t,n){return Object.is(t,n)}function us(t,n){let e=Object.create(cE);e.computation=t,n!==void 0&&(e.equal=n);let i=()=>{if(ds(e),Ai(e),e.value===za)throw e.error;return e.value};return i[yt]=e,nl(e),i}var Zc=Symbol(`UNSET`);var Jc=Symbol(`COMPUTING`);var za=Symbol(`ERRORED`);var cE=s(r({},Ha),{value:Zc,dirty:!0,error:null,equal:il,kind:`computed`,producerMustRecompute(t){return t.value===Zc||t.value===Jc},producerRecomputeValue(t){if(t.value===Jc)throw new Error(``);let n=t.value;t.value=Jc;let e=ca(t),i,a=!1;try{i=t.computation(),pe(null),a=n!==Zc&&n!==za&&i!==za&&t.equal(n,i)}catch(o){i=za,t.error=o}finally{Ua(t,e)}if(a){t.value=n;return}t.value=i,t.version++}});function lE(){throw new Error}var zb=lE;function Hb(t){zb(t)}function zm(t){zb=t}var dE=null;function Hm(t,n){let e=Object.create(Uo);e.value=t,n!==void 0&&(e.equal=n);let i=()=>Ub(e);return i[yt]=e,nl(e),[i,r=>da(e,r),r=>Um(e,r)]}function Ub(t){return Ai(t),t.value}function da(t,n){Bm()||Hb(t),t.equal(t.value,n)||(t.value=n,uE(t))}function Um(t,n){Bm()||Hb(t),da(t,n(t.value))}var Uo=s(r({},Ha),{equal:il,value:void 0,kind:`signal`});function uE(t){t.version++,Lb(),Vm(t),dE?.(t)}var $m=s(r({},Ha),{consumerIsAlwaysLive:!0,consumerAllowSignalWrites:!0,dirty:!0,kind:`effect`});function Gm(t){if(t.dirty=!1,t.version>0&&!zo(t))return;t.version++;let n=ca(t);try{t.cleanup(),t.fn()}finally{Ua(t,n)}}var Wm;function al(){return Wm}function pi(t){let n=Wm;return Wm=t,n}var $b=Symbol(`NotFound`);function $o(t){return t===$b||t?.name===`ɵNotFound`}function Gb(t){let n=pe(null);try{return t()}finally{pe(n)}}function Ie(t){return typeof t==`function`}function Go(t){let e=t(i=>{Error.call(i),i.stack=new Error().stack});return e.prototype=Object.create(Error.prototype),e.prototype.constructor=e,e}var ol=Go(t=>function(e){t(this),this.message=e?`${e.length} errors occurred during unsubscription:
${e.map((i,a)=>`${a+1}) ${i.toString()}`).join(`
  `)}`:``,this.name=`UnsubscriptionError`,this.errors=e});function $a(t,n){if(t){let e=t.indexOf(n);0<=e&&t.splice(e,1)}}var De=class t{constructor(n){this.initialTeardown=n,this.closed=!1,this._parentage=null,this._finalizers=null}unsubscribe(){let n;if(!this.closed){this.closed=!0;let{_parentage:e}=this;if(e)if(this._parentage=null,Array.isArray(e))for(let o of e)o.remove(this);else e.remove(this);let{initialTeardown:i}=this;if(Ie(i))try{i()}catch(o){n=o instanceof ol?o.errors:[o]}let{_finalizers:a}=this;if(a){this._finalizers=null;for(let o of a)try{Wb(o)}catch(r){n=n??[],r instanceof ol?n=[...n,...r.errors]:n.push(r)}}if(n)throw new ol(n)}}add(n){var e;if(n&&n!==this)if(this.closed)Wb(n);else{if(n instanceof t){if(n.closed||n._hasParent(this))return;n._addParent(this)}(this._finalizers=(e=this._finalizers)!==null&&e!==void 0?e:[]).push(n)}}_hasParent(n){let{_parentage:e}=this;return e===n||Array.isArray(e)&&e.includes(n)}_addParent(n){let{_parentage:e}=this;this._parentage=Array.isArray(e)?(e.push(n),e):e?[e,n]:n}_removeParent(n){let{_parentage:e}=this;e===n?this._parentage=null:Array.isArray(e)&&$a(e,n)}remove(n){let{_finalizers:e}=this;e&&$a(e,n),n instanceof t&&n._removeParent(this)}};De.EMPTY=(()=>{let t=new De;return t.closed=!0,t})();var qm=De.EMPTY;function rl(t){return t instanceof De||t&&`closed`in t&&Ie(t.remove)&&Ie(t.add)&&Ie(t.unsubscribe)}function Wb(t){Ie(t)?t():t.unsubscribe()}var $n={onUnhandledError:null,onStoppedNotification:null,Promise:void 0,useDeprecatedSynchronousErrorHandling:!1,useDeprecatedNextContext:!1};var Wo={setTimeout(t,n,...e){let{delegate:i}=Wo;return i?.setTimeout?i.setTimeout(t,n,...e):setTimeout(t,n,...e)},clearTimeout(t){let{delegate:n}=Wo;return(n?.clearTimeout||clearTimeout)(t)},delegate:void 0};function sl(t){Wo.setTimeout(()=>{let{onUnhandledError:n}=$n;if(n)n(t);else throw t})}function ms(){}var qb=Ym(`C`,void 0,void 0);function Yb(t){return Ym(`E`,void 0,t)}function Kb(t){return Ym(`N`,t,void 0)}function Ym(t,n,e){return{kind:t,value:n,error:e}}var Ga=null;function qo(t){if($n.useDeprecatedSynchronousErrorHandling){let n=!Ga;if(n&&(Ga={errorThrown:!1,error:null}),t(),n){let{errorThrown:e,error:i}=Ga;if(Ga=null,e)throw i}}else t()}function Qb(t){$n.useDeprecatedSynchronousErrorHandling&&Ga&&(Ga.errorThrown=!0,Ga.error=t)}var Wa=class extends De{constructor(n){super(),this.isStopped=!1,n?(this.destination=n,rl(n)&&n.add(this)):this.destination=hE}static create(n,e,i){return new Gn(n,e,i)}next(n){this.isStopped?Qm(Kb(n),this):this._next(n)}error(n){this.isStopped?Qm(Yb(n),this):(this.isStopped=!0,this._error(n))}complete(){this.isStopped?Qm(qb,this):(this.isStopped=!0,this._complete())}unsubscribe(){this.closed||(this.isStopped=!0,super.unsubscribe(),this.destination=null)}_next(n){this.destination.next(n)}_error(n){try{this.destination.error(n)}finally{this.unsubscribe()}}_complete(){try{this.destination.complete()}finally{this.unsubscribe()}}};var mE=Function.prototype.bind;function Km(t,n){return mE.call(t,n)}var Xm=class{constructor(n){this.partialObserver=n}next(n){let{partialObserver:e}=this;if(e.next)try{e.next(n)}catch(i){cl(i)}}error(n){let{partialObserver:e}=this;if(e.error)try{e.error(n)}catch(i){cl(i)}else cl(n)}complete(){let{partialObserver:n}=this;if(n.complete)try{n.complete()}catch(e){cl(e)}}};var Gn=class extends Wa{constructor(n,e,i){super();let a;if(Ie(n)||!n)a={next:n??void 0,error:e??void 0,complete:i??void 0};else{let o;this&&$n.useDeprecatedNextContext?(o=Object.create(n),o.unsubscribe=()=>this.unsubscribe(),a={next:n.next&&Km(n.next,o),error:n.error&&Km(n.error,o),complete:n.complete&&Km(n.complete,o)}):a=n}this.destination=new Xm(a)}};function cl(t){$n.useDeprecatedSynchronousErrorHandling?Qb(t):sl(t)}function pE(t){throw t}function Qm(t,n){let{onStoppedNotification:e}=$n;e&&Wo.setTimeout(()=>e(t,n))}var hE={closed:!0,next:ms,error:pE,complete:ms};var Yo=typeof Symbol==`function`&&Symbol.observable||`@@observable`;function Wn(t){return t}function Xb(t){return t.length===0?Wn:t.length===1?t[0]:function(e){return t.reduce((i,a)=>a(i),e)}}var xe=class t{constructor(n){n&&(this._subscribe=n)}lift(n){let e=new t;return e.source=this,e.operator=n,e}subscribe(n,e,i){let a=gE(n)?n:new Gn(n,e,i);return qo(()=>{let{operator:o,source:r}=this;a.add(o?o.call(a,r):r?this._subscribe(a):this._trySubscribe(a))}),a}_trySubscribe(n){try{return this._subscribe(n)}catch(e){n.error(e)}}forEach(n,e){return e=Zb(e),new e((i,a)=>{let o=new Gn({next:r=>{try{n(r)}catch(s){a(s),o.unsubscribe()}},error:a,complete:i});this.subscribe(o)})}_subscribe(n){var e;return(e=this.source)===null||e===void 0?void 0:e.subscribe(n)}[Yo](){return this}pipe(...n){return Xb(n)(this)}toPromise(n){return n=Zb(n),new n((e,i)=>{let a;this.subscribe(o=>a=o,o=>i(o),()=>e(a))})}};xe.create=t=>new xe(t);function Zb(t){var n;return(n=t??$n.Promise)!==null&&n!==void 0?n:Promise}function fE(t){return t&&Ie(t.next)&&Ie(t.error)&&Ie(t.complete)}function gE(t){return t&&t instanceof Wa||fE(t)&&rl(t)}function bE(t){return Ie(t?.lift)}function Pe(t){return n=>{if(bE(n))return n.lift(function(e){try{return t(e,this)}catch(i){this.error(i)}});throw new TypeError(`Unable to lift unknown Observable type`)}}function Ve(t,n,e,i,a){return new Zm(t,n,e,i,a)}var Zm=class extends Wa{constructor(n,e,i,a,o,r){super(n),this.onFinalize=o,this.shouldUnsubscribe=r,this._next=e?function(s){try{e(s)}catch(c){n.error(c)}}:super._next,this._error=a?function(s){try{a(s)}catch(c){n.error(c)}finally{this.unsubscribe()}}:super._error,this._complete=i?function(){try{i()}catch(s){n.error(s)}finally{this.unsubscribe()}}:super._complete}unsubscribe(){var n;if(!this.shouldUnsubscribe||this.shouldUnsubscribe()){let{closed:e}=this;super.unsubscribe(),!e&&((n=this.onFinalize)===null||n===void 0||n.call(this))}}};var Jb=Go(t=>function(){t(this),this.name=`ObjectUnsubscribedError`,this.message=`object unsubscribed`});var L=class extends xe{constructor(){super(),this.closed=!1,this.currentObservers=null,this.observers=[],this.isStopped=!1,this.hasError=!1,this.thrownError=null}lift(n){let e=new ll(this,this);return e.operator=n,e}_throwIfClosed(){if(this.closed)throw new Jb}next(n){qo(()=>{if(this._throwIfClosed(),!this.isStopped){this.currentObservers||(this.currentObservers=Array.from(this.observers));for(let e of this.currentObservers)e.next(n)}})}error(n){qo(()=>{if(this._throwIfClosed(),!this.isStopped){this.hasError=this.isStopped=!0,this.thrownError=n;let{observers:e}=this;for(;e.length;)e.shift().error(n)}})}complete(){qo(()=>{if(this._throwIfClosed(),!this.isStopped){this.isStopped=!0;let{observers:n}=this;for(;n.length;)n.shift().complete()}})}unsubscribe(){this.isStopped=this.closed=!0,this.observers=this.currentObservers=null}get observed(){var n;return((n=this.observers)===null||n===void 0?void 0:n.length)>0}_trySubscribe(n){return this._throwIfClosed(),super._trySubscribe(n)}_subscribe(n){return this._throwIfClosed(),this._checkFinalizedStatuses(n),this._innerSubscribe(n)}_innerSubscribe(n){let{hasError:e,isStopped:i,observers:a}=this;return e||i?qm:(this.currentObservers=null,a.push(n),new De(()=>{this.currentObservers=null,$a(a,n)}))}_checkFinalizedStatuses(n){let{hasError:e,thrownError:i,isStopped:a}=this;e?n.error(i):a&&n.complete()}asObservable(){let n=new xe;return n.source=this,n}};L.create=(t,n)=>new ll(t,n);var ll=class extends L{constructor(n,e){super(),this.destination=n,this.source=e}next(n){var e,i;(i=(e=this.destination)===null||e===void 0?void 0:e.next)===null||i===void 0||i.call(e,n)}error(n){var e,i;(i=(e=this.destination)===null||e===void 0?void 0:e.error)===null||i===void 0||i.call(e,n)}complete(){var n,e;(e=(n=this.destination)===null||n===void 0?void 0:n.complete)===null||e===void 0||e.call(n)}_subscribe(n){var e,i;return(i=(e=this.source)===null||e===void 0?void 0:e.subscribe(n))!==null&&i!==void 0?i:qm}};var hi=class extends L{constructor(n){super(),this._value=n}get value(){return this.getValue()}_subscribe(n){let e=super._subscribe(n);return!e.closed&&n.next(this._value),e}getValue(){let{hasError:n,thrownError:e,_value:i}=this;if(n)throw e;return this._throwIfClosed(),i}next(n){super.next(this._value=n)}};var ps={now(){return(ps.delegate||Date).now()},delegate:void 0};var qn=class extends L{constructor(n=1/0,e=1/0,i=ps){super(),this._bufferSize=n,this._windowTime=e,this._timestampProvider=i,this._buffer=[],this._infiniteTimeWindow=!0,this._infiniteTimeWindow=e===1/0,this._bufferSize=Math.max(1,n),this._windowTime=Math.max(1,e)}next(n){let{isStopped:e,_buffer:i,_infiniteTimeWindow:a,_timestampProvider:o,_windowTime:r}=this;e||(i.push(n),!a&&i.push(o.now()+r)),this._trimBuffer(),super.next(n)}_subscribe(n){this._throwIfClosed(),this._trimBuffer();let e=this._innerSubscribe(n),{_infiniteTimeWindow:i,_buffer:a}=this,o=a.slice();for(let r=0;r<o.length&&!n.closed;r+=i?1:2)n.next(o[r]);return this._checkFinalizedStatuses(n),e}_trimBuffer(){let{_bufferSize:n,_timestampProvider:e,_buffer:i,_infiniteTimeWindow:a}=this,o=(a?1:2)*n;if(n<1/0&&o<i.length&&i.splice(0,i.length-o),!a){let r=e.now(),s=0;for(let c=1;c<i.length&&i[c]<=r;c+=2)s=c;s&&i.splice(0,s+1)}}};var dl=class extends De{constructor(n,e){super()}schedule(n,e=0){return this}};var hs={setInterval(t,n,...e){let{delegate:i}=hs;return i?.setInterval?i.setInterval(t,n,...e):setInterval(t,n,...e)},clearInterval(t){let{delegate:n}=hs;return(n?.clearInterval||clearInterval)(t)},delegate:void 0};var ul=class extends dl{constructor(n,e){super(n,e),this.scheduler=n,this.work=e,this.pending=!1}schedule(n,e=0){var i;if(this.closed)return this;this.state=n;let a=this.id,o=this.scheduler;return a!=null&&(this.id=this.recycleAsyncId(o,a,e)),this.pending=!0,this.delay=e,this.id=(i=this.id)!==null&&i!==void 0?i:this.requestAsyncId(o,this.id,e),this}requestAsyncId(n,e,i=0){return hs.setInterval(n.flush.bind(n,this),i)}recycleAsyncId(n,e,i=0){if(i!=null&&this.delay===i&&this.pending===!1)return e;e!=null&&hs.clearInterval(e)}execute(n,e){if(this.closed)return new Error(`executing a cancelled action`);this.pending=!1;let i=this._execute(n,e);if(i)return i;this.pending===!1&&this.id!=null&&(this.id=this.recycleAsyncId(this.scheduler,this.id,null))}_execute(n,e){let i=!1,a;try{this.work(n)}catch(o){i=!0,a=o||new Error(`Scheduled action threw falsy error`)}if(i)return this.unsubscribe(),a}unsubscribe(){if(!this.closed){let{id:n,scheduler:e}=this,{actions:i}=e;this.work=this.state=this.scheduler=null,this.pending=!1,$a(i,this),n!=null&&(this.id=this.recycleAsyncId(e,n,null)),this.delay=null,super.unsubscribe()}}};var Ko=class t{constructor(n,e=t.now){this.schedulerActionCtor=n,this.now=e}schedule(n,e=0,i){return new this.schedulerActionCtor(this,n).schedule(i,e)}};Ko.now=ps.now;var ml=class extends Ko{constructor(n,e=Ko.now){super(n,e),this.actions=[],this._active=!1}flush(n){let{actions:e}=this;if(this._active){e.push(n);return}let i;this._active=!0;do if(i=n.execute(n.state,n.delay))break;while(n=e.shift());if(this._active=!1,i){for(;n=e.shift();)n.unsubscribe();throw i}}};var fs=new ml(ul);var ev=fs;var Oi=new xe(t=>t.complete());function pl(t){return t&&Ie(t.schedule)}function Jm(t){return t[t.length-1]}function hl(t){return Ie(Jm(t))?t.pop():void 0}function fi(t){return pl(Jm(t))?t.pop():void 0}function tv(t,n){return typeof Jm(t)==`number`?t.pop():n}function iv(t,n,e,i){function a(o){return o instanceof e?o:new e(function(r){r(o)})}return new(e||(e=Promise))(function(o,r){function s(u){try{m(i.next(u))}catch(g){r(g)}}function c(u){try{m(i.throw(u))}catch(g){r(g)}}function m(u){u.done?o(u.value):a(u.value).then(s,c)}m((i=i.apply(t,n||[])).next())})}function nv(t){var n=typeof Symbol==`function`&&Symbol.iterator,e=n&&t[n],i=0;if(e)return e.call(t);if(t&&typeof t.length==`number`)return{next:function(){return t&&i>=t.length&&(t=void 0),{value:t&&t[i++],done:!t}}};throw new TypeError(n?`Object is not iterable.`:`Symbol.iterator is not defined.`)}function qa(t){return this instanceof qa?(this.v=t,this):new qa(t)}function av(t,n,e){if(!Symbol.asyncIterator)throw new TypeError(`Symbol.asyncIterator is not defined.`);var i=e.apply(t,n||[]),a,o=[];return a=Object.create((typeof AsyncIterator==`function`?AsyncIterator:Object).prototype),s(`next`),s(`throw`),s(`return`,r),a[Symbol.asyncIterator]=function(){return this},a;function r(b){return function(y){return Promise.resolve(y).then(b,g)}}function s(b,y){i[b]&&(a[b]=function(E){return new Promise(function(R,oe){o.push([b,E,R,oe])>1||c(b,E)})},y&&(a[b]=y(a[b])))}function c(b,y){try{m(i[b](y))}catch(E){v(o[0][3],E)}}function m(b){b.value instanceof qa?Promise.resolve(b.value.v).then(u,g):v(o[0][2],b)}function u(b){c(`next`,b)}function g(b){c(`throw`,b)}function v(b,y){b(y),o.shift(),o.length&&c(o[0][0],o[0][1])}}function ov(t){if(!Symbol.asyncIterator)throw new TypeError(`Symbol.asyncIterator is not defined.`);var n=t[Symbol.asyncIterator],e;return n?n.call(t):(t=typeof nv==`function`?nv(t):t[Symbol.iterator](),e={},i(`next`),i(`throw`),i(`return`),e[Symbol.asyncIterator]=function(){return this},e);function i(o){e[o]=t[o]&&function(r){return new Promise(function(s,c){r=t[o](r),a(s,c,r.done,r.value)})}}function a(o,r,s,c){Promise.resolve(c).then(function(m){o({value:m,done:s})},r)}}var fl=(t=>t&&typeof t.length==`number`&&typeof t!=`function`);function gl(t){return Ie(t?.then)}function bl(t){return Ie(t[Yo])}function vl(t){return Symbol.asyncIterator&&Ie(t?.[Symbol.asyncIterator])}function yl(t){return new TypeError(`You provided ${t!==null&&typeof t==`object`?`an invalid object`:`'${t}'`} where a stream was expected. You can provide an Observable, Promise, ReadableStream, Array, AsyncIterable, or Iterable.`)}function vE(){return typeof Symbol!=`function`||!Symbol.iterator?`@@iterator`:Symbol.iterator}var _l=vE();function xl(t){return Ie(t?.[_l])}function Cl(t){return av(this,arguments,function*(){let e=t.getReader();try{for(;;){let{value:i,done:a}=yield qa(e.read());if(a)return yield qa(void 0);yield yield qa(i)}}finally{e.releaseLock()}})}function Sl(t){return Ie(t?.getReader)}function lt(t){if(t instanceof xe)return t;if(t!=null){if(bl(t))return yE(t);if(fl(t))return _E(t);if(gl(t))return xE(t);if(vl(t))return rv(t);if(xl(t))return CE(t);if(Sl(t))return SE(t)}throw yl(t)}function yE(t){return new xe(n=>{let e=t[Yo]();if(Ie(e.subscribe))return e.subscribe(n);throw new TypeError(`Provided object does not correctly implement Symbol.observable`)})}function _E(t){return new xe(n=>{for(let e=0;e<t.length&&!n.closed;e++)n.next(t[e]);n.complete()})}function xE(t){return new xe(n=>{t.then(e=>{n.closed||(n.next(e),n.complete())},e=>n.error(e)).then(null,sl)})}function CE(t){return new xe(n=>{for(let e of t)if(n.next(e),n.closed)return;n.complete()})}function rv(t){return new xe(n=>{wE(t,n).catch(e=>n.error(e))})}function SE(t){return rv(Cl(t))}function wE(t,n){var e,i,a,o;return iv(this,void 0,void 0,function*(){try{for(e=ov(t);i=yield e.next(),!i.done;){let r=i.value;if(n.next(r),n.closed)return}}catch(r){a={error:r}}finally{try{i&&!i.done&&(o=e.return)&&(yield o.call(e))}finally{if(a)throw a.error}}n.complete()})}function ln(t,n,e,i=0,a=!1){let o=n.schedule(function(){e(),a?t.add(this.schedule(null,i)):this.unsubscribe()},i);if(t.add(o),!a)return o}function wl(t,n=0){return Pe((e,i)=>{e.subscribe(Ve(i,a=>ln(i,t,()=>i.next(a),n),()=>ln(i,t,()=>i.complete(),n),a=>ln(i,t,()=>i.error(a),n)))})}function El(t,n=0){return Pe((e,i)=>{i.add(t.schedule(()=>e.subscribe(i),n))})}function sv(t,n){return lt(t).pipe(El(n),wl(n))}function cv(t,n){return lt(t).pipe(El(n),wl(n))}function lv(t,n){return new xe(e=>{let i=0;return n.schedule(function(){i===t.length?e.complete():(e.next(t[i++]),e.closed||this.schedule())})})}function dv(t,n){return new xe(e=>{let i;return ln(e,n,()=>{i=t[_l](),ln(e,n,()=>{let a,o;try{({value:a,done:o}=i.next())}catch(r){e.error(r);return}o?e.complete():e.next(a)},0,!0)}),()=>Ie(i?.return)&&i.return()})}function Dl(t,n){if(!t)throw new Error(`Iterable cannot be null`);return new xe(e=>{ln(e,n,()=>{let i=t[Symbol.asyncIterator]();ln(e,n,()=>{i.next().then(a=>{a.done?e.complete():e.next(a.value)})},0,!0)})})}function uv(t,n){return Dl(Cl(t),n)}function mv(t,n){if(t!=null){if(bl(t))return sv(t,n);if(fl(t))return lv(t,n);if(gl(t))return cv(t,n);if(vl(t))return Dl(t,n);if(xl(t))return dv(t,n);if(Sl(t))return uv(t,n)}throw yl(t)}function kn(t,n){return n?mv(t,n):lt(t)}function In(...t){return kn(t,fi(t))}var pv=Go(t=>function(){t(this),this.name=`EmptyError`,this.message=`no elements in sequence`});function At(t,n){let e=typeof n==`object`;return new Promise((i,a)=>{let o=new Gn({next:r=>{i(r),o.unsubscribe()},error:a,complete:()=>{e?i(n.defaultValue):a(new pv)}});t.subscribe(o)})}function hv(t){return t instanceof Date&&!isNaN(t)}function ot(t,n){return Pe((e,i)=>{let a=0;e.subscribe(Ve(i,o=>{i.next(t.call(n,o,a++))}))})}var{isArray:EE}=Array;function DE(t,n){return EE(n)?t(...n):t(n)}function kl(t){return ot(n=>DE(t,n))}var{isArray:kE}=Array,{getPrototypeOf:IE,prototype:ME,keys:NE}=Object;function Il(t){if(t.length===1){let n=t[0];if(kE(n))return{args:n,keys:null};if(TE(n)){let e=NE(n);return{args:e.map(i=>n[i]),keys:e}}}return{args:t,keys:null}}function TE(t){return t&&typeof t==`object`&&IE(t)===ME}function Ml(t,n){return t.reduce((e,i,a)=>(e[i]=n[a],e),{})}function ep(...t){let n=fi(t),e=hl(t),{args:i,keys:a}=Il(t);if(i.length===0)return kn([],n);let o=new xe(AE(i,n,a?r=>Ml(a,r):Wn));return e?o.pipe(kl(e)):o}function AE(t,n,e=Wn){return i=>{fv(n,()=>{let{length:a}=t,o=new Array(a),r=a,s=a;for(let c=0;c<a;c++)fv(n,()=>{let m=kn(t[c],n),u=!1;m.subscribe(Ve(i,g=>{o[c]=g,u||(u=!0,s--),s||i.next(e(o.slice()))},()=>{--r||i.complete()}))},i)},i)}}function fv(t,n,e){t?ln(e,t,n):n()}function gv(t,n,e,i,a,o,r,s){let c=[],m=0,u=0,g=!1,v=()=>{g&&!c.length&&!m&&n.complete()},b=E=>m<i?y(E):c.push(E),y=E=>{o&&n.next(E),m++;let R=!1;lt(e(E,u++)).subscribe(Ve(n,oe=>{a?.(oe),o?b(oe):n.next(oe)},()=>{R=!0},void 0,()=>{if(R)try{for(m--;c.length&&m<i;){let oe=c.shift();r?ln(n,r,()=>y(oe)):y(oe)}v()}catch(oe){n.error(oe)}}))};return t.subscribe(Ve(n,b,()=>{g=!0,v()})),()=>{s?.()}}function Qo(t,n,e=1/0){return Ie(n)?Qo((i,a)=>ot((o,r)=>n(i,o,a,r))(lt(t(i,a))),e):(typeof n==`number`&&(e=n),Pe((i,a)=>gv(i,a,t,e)))}function Nl(t=1/0){return Qo(Wn,t)}function bv(){return Nl(1)}function Xo(...t){return bv()(kn(t,fi(t)))}function Ya(t){return new xe(n=>{lt(t()).subscribe(n)})}function tp(...t){let n=hl(t),{args:e,keys:i}=Il(t),a=new xe(o=>{let{length:r}=e;if(!r){o.complete();return}let s=new Array(r),c=r,m=r;for(let u=0;u<r;u++){let g=!1;lt(e[u]).subscribe(Ve(o,v=>{g||(g=!0,m--),s[u]=v},()=>c--,void 0,()=>{(!c||!g)&&(m||o.next(i?Ml(i,s):s),o.complete())}))}});return n?a.pipe(kl(n)):a}function gs(t=0,n,e=ev){let i=-1;return n!=null&&(pl(n)?e=n:i=n),new xe(a=>{let o=hv(t)?+t-e.now():t;o<0&&(o=0);let r=0;return e.schedule(function(){a.closed||(a.next(r++),0<=i?this.schedule(void 0,i):a.complete())},o)})}function wt(...t){let n=fi(t),e=tv(t,1/0),i=t;return i.length?i.length===1?lt(i[0]):Nl(e)(kn(i,n)):Oi}function Ke(t,n){return Pe((e,i)=>{let a=0;e.subscribe(Ve(i,o=>t.call(n,o,a++)&&i.next(o)))})}function vv(t){return Pe((n,e)=>{let i=!1,a=null,o=null,r=!1,s=()=>{if(o?.unsubscribe(),o=null,i){i=!1;let m=a;a=null,e.next(m)}r&&e.complete()},c=()=>{o=null,r&&e.complete()};n.subscribe(Ve(e,m=>{i=!0,a=m,o||lt(t(m)).subscribe(o=Ve(e,s,c))},()=>{r=!0,(!i||!o||o.closed)&&e.complete()}))})}function Tl(t,n=fs){return vv(()=>gs(t,n))}function np(t,n){return Ie(n)?Qo(t,n,1):Qo(t,1)}function Ri(t,n=fs){return Pe((e,i)=>{let a=null,o=null,r=null,s=()=>{if(a){a.unsubscribe(),a=null;let m=o;o=null,i.next(m)}};function c(){let m=r+t,u=n.now();if(u<m){a=this.schedule(void 0,m-u),i.add(a);return}s()}e.subscribe(Ve(i,m=>{o=m,r=n.now(),a||(a=n.schedule(c,t),i.add(a))},()=>{s(),i.complete()},void 0,()=>{o=a=null}))})}function hn(t){return t<=0?()=>Oi:Pe((n,e)=>{let i=0;n.subscribe(Ve(e,a=>{++i<=t&&(e.next(a),t<=i&&e.complete())}))})}function Al(t,n=Wn){return t=t??OE,Pe((e,i)=>{let a,o=!0;e.subscribe(Ve(i,r=>{let s=n(r);(o||!t(a,s))&&(o=!1,a=s,i.next(r))}))})}function OE(t,n){return t===n}function ip(t){return Pe((n,e)=>{try{n.subscribe(e)}finally{e.add(t)}})}function Ol(){return Pe((t,n)=>{let e,i=!1;t.subscribe(Ve(n,a=>{let o=e;e=a,i&&n.next([o,a]),i=!0}))})}function yv(t={}){let{connector:n=()=>new L,resetOnError:e=!0,resetOnComplete:i=!0,resetOnRefCountZero:a=!0}=t;return o=>{let r,s,c,m=0,u=!1,g=!1,v=()=>{s?.unsubscribe(),s=void 0},b=()=>{v(),r=c=void 0,u=g=!1},y=()=>{let E=r;b(),E?.unsubscribe()};return Pe((E,R)=>{m++,!g&&!u&&v();let oe=c=c??n();R.add(()=>{m--,m===0&&!g&&!u&&(s=ap(y,a))}),oe.subscribe(R),!r&&m>0&&(r=new Gn({next:j=>oe.next(j),error:j=>{g=!0,v(),s=ap(b,e,j),oe.error(j)},complete:()=>{u=!0,v(),s=ap(b,i),oe.complete()}}),lt(E).subscribe(r))})(o)}}function ap(t,n,...e){if(n===!0){t();return}if(n===!1)return;let i=new Gn({next:()=>{i.unsubscribe(),t()}});return lt(n(...e)).subscribe(i)}function Rl(t,n,e){let i,a=!1;return t&&typeof t==`object`?{bufferSize:i=1/0,windowTime:n=1/0,refCount:a=!1,scheduler:e}=t:i=t??1/0,yv({connector:()=>new qn(i,n,e),resetOnError:!0,resetOnComplete:!1,resetOnRefCountZero:a})}function Ka(t){return Ke((n,e)=>t<=e)}function dt(...t){let n=fi(t);return Pe((e,i)=>{(n?Xo(t,e,n):Xo(t,e)).subscribe(i)})}function fn(t,n){return Pe((e,i)=>{let a=null,o=0,r=!1,s=()=>r&&!a&&i.complete();e.subscribe(Ve(i,c=>{a?.unsubscribe();let m=0,u=o++;lt(t(c,u)).subscribe(a=Ve(i,g=>i.next(n?n(c,g,u,m++):g),()=>{a=null,s()}))},()=>{r=!0,s()}))})}function Me(t){return Pe((n,e)=>{lt(t).subscribe(Ve(e,()=>e.complete(),ms)),!e.closed&&n.subscribe(e)})}function op(t,n=!1){return Pe((e,i)=>{let a=0;e.subscribe(Ve(i,o=>{let r=t(o,a++);(r||n)&&i.next(o),!r&&i.complete()}))})}function rp(t,n,e){let i=Ie(t)||n||e?{next:t,error:n,complete:e}:t;return i?Pe((a,o)=>{var r;(r=i.subscribe)===null||r===void 0||r.call(i);let s=!0;a.subscribe(Ve(o,c=>{var m;(m=i.next)===null||m===void 0||m.call(i,c),o.next(c)},()=>{var c;s=!1,(c=i.complete)===null||c===void 0||c.call(i),o.complete()},c=>{var m;s=!1,(m=i.error)===null||m===void 0||m.call(i,c),o.error(c)},()=>{var c,m;s&&((c=i.unsubscribe)===null||c===void 0||c.call(i)),(m=i.finalize)===null||m===void 0||m.call(i)}))}):Wn}var zl=`https://angular.dev/best-practices/security#preventing-cross-site-scripting-xss`;var $=class extends Error{code;constructor(n,e){super(Vi(n,e)),this.code=n}};function RE(t){return`NG0${Math.abs(t)}`}function Vi(t,n){return`${RE(t)}${n?`: `+n:``}`}function qe(t){for(let n in t)if(t[n]===qe)return n;throw Error(``)}function wv(t,n){for(let e in n)Object.hasOwn(n,e)&&!Object.hasOwn(t,e)&&(t[e]=n[e])}function Cs(t){if(typeof t==`string`)return t;if(Array.isArray(t))return`[${t.map(Cs).join(`, `)}]`;if(t==null)return``+t;let n=t.overriddenName||t.name;if(n)return`${n}`;let e=t.toString();if(e==null)return``+e;let i=e.indexOf(`
`);return i>=0?e.slice(0,i):e}function Hl(t,n){return t?n?`${t} ${n}`:t:n||``}var PE=qe({__forward_ref__:qe});function zt(t){return t.__forward_ref__=zt,t}function Ot(t){return _p(t)?t():t}function _p(t){return typeof t==`function`&&Object.hasOwn(t,PE)&&t.__forward_ref__===zt}function he(t){return{token:t.token,providedIn:t.providedIn||null,factory:t.factory,value:void 0}}function te(t){return{providers:t.providers||[],imports:t.imports||[]}}function Ul(t){return FE(t,$l)}function FE(t,n){return Object.hasOwn(t,n)&&t[n]||null}function LE(t){return(t?.[$l]??null)||null}function cp(t){return t&&Object.hasOwn(t,Fl)?t[Fl]:null}var $l=qe({ɵprov:qe});var Fl=qe({ɵinj:qe});var D=class{_desc;ngMetadataName=`InjectionToken`;ɵprov;constructor(n,e){this._desc=n,this.ɵprov=void 0,typeof e==`number`?this.__NG_ELEMENT_ID__=e:e!==void 0&&(this.ɵprov=he({token:this,providedIn:e.providedIn||`root`,factory:e.factory}))}get multi(){return this}toString(){return`InjectionToken ${this._desc}`}};function xp(t){return t&&!!t.ɵproviders}var Ss=qe({ɵcmp:qe});var ws=qe({ɵdir:qe});var Cp=qe({ɵpipe:qe});var Sp=qe({ɵmod:qe});var vs=qe({ɵfac:qe});var eo=qe({__NG_ELEMENT_ID__:qe});var _v=qe({__NG_ENV_ID__:qe});function Ev(t){return Gl(t,`@NgModule`),t[Sp]||null}function ma(t){return Gl(t,`@Component`),t[Ss]||null}function Es(t){return Gl(t,`@Directive`),t[ws]||null}function Dv(t){return Gl(t,`@Pipe`),t[Cp]||null}function Gl(t,n){if(t==null)throw new $(-919,!1)}function Rt(t){return typeof t==`string`?t:t==null?``:String(t)}var kv=qe({ngErrorCode:qe});var VE=qe({ngErrorMessage:qe});var BE=qe({ngTokenPath:qe});function wp(t,n){return Iv(``,-200,n)}function Wl(t,n){throw new $(-201,!1)}function Iv(t,n,e){let i=new $(n,t);return i[kv]=n,i[VE]=t,e&&(i[BE]=e),i}function jE(t){return t[kv]}var lp;function Mv(){return lp}function nn(t){let n=lp;return lp=t,n}function Ep(t,n,e){let i=Ul(t);if(i&&i.providedIn==`root`)return i.value===void 0?i.value=i.factory():i.value;if(e&8)return null;if(n!==void 0)return n;Wl(t,``)}var bi=globalThis;var Qa={};var HE=`__NG_DI_FLAG__`;var dp=class{injector;constructor(n){this.injector=n}retrieve(n,e){let i=Xa(e)||0;try{return this.injector.get(n,i&8?null:Qa,i)}catch(a){if($o(a))return a;throw a}}};function UE(t,n=0){let e=al();if(e===void 0)throw new $(-203,!1);if(e===null)return Ep(t,void 0,n);{let i=$E(n),a=e.retrieve(t,i);if($o(a)){if(i.optional)return null;throw a}return a}}function Se(t,n=0){return(Mv()||UE)(Ot(t),n)}function f(t,n){return Se(t,Xa(n))}function Xa(t){return typeof t>`u`||typeof t==`number`?t:0|(t.optional&&8)|(t.host&&1)|(t.self&&2)|(t.skipSelf&&4)}function $E(t){return{optional:!!(t&8),host:!!(t&1),self:!!(t&2),skipSelf:!!(t&4)}}function up(t){let n=[];for(let e=0;e<t.length;e++){let i=Ot(t[e]);if(Array.isArray(i)){if(i.length===0)throw new $(900,!1);let a,o=0;for(let r=0;r<i.length;r++){let s=i[r],c=GE(s);typeof c==`number`?c===-1?a=s.token:o|=c:a=s}n.push(Se(a,o))}else n.push(Se(i))}return n}function GE(t){return t[HE]}function ua(t,n){return Object.hasOwn(t,vs)?t[vs]:null}function Nv(t,n,e){if(t.length!==n.length)return!1;for(let i=0;i<t.length;i++){let a=t[i],o=n[i];if(e&&(a=e(a),o=e(o)),o!==a)return!1}return!0}function Tv(t){return t.flat(Number.POSITIVE_INFINITY)}function ql(t,n){t.forEach(e=>Array.isArray(e)?ql(e,n):n(e))}function Dp(t,n,e){n>=t.length?t.push(e):t.splice(n,0,e)}function Ds(t,n){return n>=t.length-1?t.pop():t.splice(n,1)[0]}function Av(t,n){let e=[];for(let i=0;i<t;i++)e.push(n);return e}function Ov(t,n,e,i){let a=t.length;if(a==n)t.push(e,i);else if(a===1)t.push(i,t[0]),t[0]=e;else{for(a--,t.push(t[a-1],t[a]);a>n;){let o=a-2;t[a]=t[o],a--}t[n]=e,t[n+1]=i}}function Yl(t,n,e){let i=er(t,n);return i>=0?t[i|1]=e:(i=~i,Ov(t,i,n,e)),i}function Kl(t,n){let e=er(t,n);if(e>=0)return t[e|1]}function er(t,n){return WE(t,n,1)}function WE(t,n,e){let i=0,a=t.length>>e;for(;a!==i;){let o=i+(a-i>>1),r=t[o<<e];if(n===r)return o<<e;r>n?a=o:i=o+1}return~(a<<e)}var pa={};var Yt=[];var to=new D(``);var Ql=new D(``,-1);var kp=new D(``);var Jo=class{get(n,e=Qa){if(e===Qa){let a=Iv(``,-201);throw a.name=`ɵNotFound`,a}return e}};function an(t){return{ɵproviders:t}}function Rv(t){return an([{provide:to,multi:!0,useValue:t}])}function Pv(...t){return{ɵproviders:Ip(!0,t),ɵfromNgModule:!0}}function Ip(t,...n){let e=[],i=new Set,a,o=r=>{e.push(r)};return ql(n,r=>{let s=r;Ll(s,o,[],i)&&(a||=[],a.push(s))}),a!==void 0&&Fv(a,o),e}function Fv(t,n){for(let e=0;e<t.length;e++){let{ngModule:i,providers:a}=t[e];Mp(a,o=>{n(o,i)})}}function Ll(t,n,e,i){if(t=Ot(t),!t)return!1;let a=null,o=cp(t),r=!o&&ma(t);if(!o&&!r){let c=t.ngModule;if(o=cp(c),o)a=c;else return!1}else{if(r&&!r.standalone)return!1;a=t}let s=i.has(a);if(r){if(s)return!1;if(i.add(a),r.dependencies){let c=typeof r.dependencies==`function`?r.dependencies():r.dependencies;for(let m of c)Ll(m,n,e,i)}}else if(o){if(o.imports!=null&&!s){i.add(a);let m;ql(o.imports,u=>{Ll(u,n,e,i)&&(m||=[],m.push(u))}),m!==void 0&&Fv(m,n)}if(!s){let m=ua(a)||(()=>new a);n({provide:a,useFactory:m,deps:Yt},a),n({provide:kp,useValue:a,multi:!0},a),n({provide:to,useValue:()=>Se(a),multi:!0},a)}let c=o.providers;if(c!=null&&!s){let m=t;Mp(c,u=>{n(u,m)})}}else return!1;return a!==t&&t.providers!==void 0}function Mp(t,n){for(let e of t)xp(e)&&(e=e.ɵproviders),Array.isArray(e)?Mp(e,n):n(e)}var qE=qe({provide:String,useValue:qe});function Lv(t){return t!==null&&typeof t==`object`&&qE in t}function YE(t){return!!(t&&t.useExisting)}function KE(t){return!!(t&&t.useFactory)}function Za(t){return typeof t==`function`}function Vv(t){return!!t.useClass}var ks=new D(``);var Pl={};var xv={};var sp;function tr(){return sp===void 0&&(sp=new Jo),sp}var Et=class{};var Ja=class extends Et{parent;source;scopes;records=new Map;_ngOnDestroyHooks=new Set;_onDestroyHooks=[];get destroyed(){return this._destroyed}_destroyed=!1;injectorDefTypes;constructor(n,e,i,a){super(),this.parent=e,this.source=i,this.scopes=a,pp(n,r=>this.processProvider(r)),this.records.set(Ql,Zo(void 0,this)),a.has(`environment`)&&this.records.set(Et,Zo(void 0,this));let o=this.records.get(ks);o!=null&&typeof o.value==`string`&&this.scopes.add(o.value),this.injectorDefTypes=new Set(this.get(kp,Yt,{self:!0}))}retrieve(n,e){let i=Xa(e)||0;try{return this.get(n,Qa,i)}catch(a){if($o(a))return a;throw a}}destroy(){bs(this),this._destroyed=!0;let n=pe(null);try{for(let i of this._ngOnDestroyHooks)i.ngOnDestroy();let e=this._onDestroyHooks;this._onDestroyHooks=[];for(let i of e)i()}finally{this.records.clear(),this._ngOnDestroyHooks.clear(),this.injectorDefTypes.clear(),pe(n)}}onDestroy(n){return bs(this),this._onDestroyHooks.push(n),()=>this.removeOnDestroy(n)}runInContext(n){bs(this);let e=pi(this),i=nn(void 0);try{return n()}finally{pi(e),nn(i)}}get(n,e=Qa,i){if(bs(this),Object.hasOwn(n,_v))return n[_v](this);let a=Xa(i),r=pi(this),s=nn(void 0);try{if(!(a&4)){let m=this.records.get(n);if(m===void 0){let u=eD(n)&&Ul(n);u&&this.injectableDefInScope(u)?m=Zo(mp(n),Pl):m=null,this.records.set(n,m)}if(m!=null)return this.hydrate(n,m,a)}let c=a&2?tr():this.parent;return e=a&8&&e===Qa?null:e,c.get(n,e)}catch(c){let m=jE(c);throw m===-200||m===-201?new $(m,null):c}finally{nn(s),pi(r)}}resolveInjectorInitializers(){let n=pe(null),e=pi(this),i=nn(void 0);try{let o=this.get(to,Yt,{self:!0});for(let r of o)r()}finally{pi(e),nn(i),pe(n)}}toString(){return`R3Injector[...]`}processProvider(n){n=Ot(n);let e=Za(n)?n:Ot(n&&n.provide),i=XE(n);if(!Za(n)&&n.multi===!0){let a=this.records.get(e);a||(a=Zo(void 0,Pl,!0),a.factory=()=>up(a.multi),this.records.set(e,a)),e=n,a.multi.push(n)}this.records.set(e,i)}hydrate(n,e,i){let a=pe(null);try{if(e.value===xv)throw wp(``);return e.value===Pl&&(e.value=xv,e.value=e.factory(void 0,i)),typeof e.value==`object`&&e.value&&JE(e.value)&&this._ngOnDestroyHooks.add(e.value),e.value}finally{pe(a)}}injectableDefInScope(n){if(!n.providedIn)return!1;let e=Ot(n.providedIn);return typeof e==`string`?e===`any`||this.scopes.has(e):this.injectorDefTypes.has(e)}removeOnDestroy(n){let e=this._onDestroyHooks.indexOf(n);e!==-1&&this._onDestroyHooks.splice(e,1)}};function mp(t){let n=Ul(t),e=n!==null?n.factory:ua(t);if(e!==null)return e;if(t instanceof D)throw new $(-204,!1);if(t instanceof Function)return QE(t);throw new $(-204,!1)}function QE(t){if(t.length>0)throw new $(-204,!1);let e=LE(t);return e!==null?()=>e.factory(t):()=>new t}function XE(t){if(Lv(t))return Zo(void 0,t.useValue);return Zo(Np(t),Pl)}function Np(t,n,e){let i;if(Za(t)){let a=Ot(t);return ua(a)||mp(a)}else if(Lv(t))i=()=>Ot(t.useValue);else if(KE(t))i=()=>t.useFactory(...up(t.deps||[]));else if(YE(t))i=(a,o)=>Se(Ot(t.useExisting),o!==void 0&&o&8?8:void 0);else{let a=Ot(t&&(t.useClass||t.provide));if(ZE(t))i=()=>new a(...up(t.deps));else return ua(a)||mp(a)}return i}function bs(t){if(t.destroyed)throw new $(-205,!1)}function Zo(t,n,e=!1){return{factory:t,value:n,multi:e?[]:void 0}}function ZE(t){return!!t.deps}function JE(t){return t!==null&&typeof t==`object`&&typeof t.ngOnDestroy==`function`}function eD(t){return typeof t==`function`||typeof t==`object`&&t.ngMetadataName===`InjectionToken`}function pp(t,n){for(let e of t)Array.isArray(e)?pp(e,n):e&&xp(e)?pp(e.ɵproviders,n):n(e)}function nr(t,n){let e;t instanceof Ja?(bs(t),e=t):e=new dp(t);let a=pi(e),o=nn(void 0);try{return n()}finally{pi(a),nn(o)}}function Bv(){return Mv()!==void 0||al()!=null}var Yn=0;var fe=1;var _e=2;var _t=3;var Nn=4;var Ht=5;var no=6;var ir=7;var gt=8;var Bi=9;var Kn=10;var Je=11;var ar=12;var Tp=13;var ha=14;var Kt=15;var fa=16;var io=17;var vi=18;var Is=19;var Ap=20;var Pi=21;var Xl=22;var Fi=23;var gn=24;var ao=25;var jv=26;var or=27;var it=28;var zv=1;var Op=6;var oo=7;var Ms=8;var ro=9;var bt=10;function ji(t){return Array.isArray(t)&&typeof t[zv]==`object`}function Tn(t){return Array.isArray(t)&&t[zv]===!0}function Rp(t){return(t.flags&4)!==0}function yi(t){return t.componentOffset>-1}function rr(t){return(t.flags&1)===1}function Qn(t){return!!t.template}function sr(t){return(t[_e]&512)!==0}function so(t){return(t[_e]&256)===256}var bn=(function(t){return t[t.NONE=0]=`NONE`,t[t.HTML=1]=`HTML`,t[t.STYLE=2]=`STYLE`,t[t.SCRIPT=3]=`SCRIPT`,t[t.URL=4]=`URL`,t[t.RESOURCE_URL=5]=`RESOURCE_URL`,t[t.ATTRIBUTE_NO_BINDING=6]=`ATTRIBUTE_NO_BINDING`,t})(bn||{});var Pp=`svg`;var Hv=`math`;function Ut(t){for(;Array.isArray(t);)t=t[Yn];return t}function Fp(t,n){return Ut(n[t])}function vn(t,n){return Ut(n[t.index])}function Zl(t,n){return t.data[n]}function Ns(t,n){return t[n]}function Ts(t,n,e,i){e>=t.data.length&&(t.data[e]=null,t.blueprint[e]=null),n[e]=i}function An(t,n){let e=n[t];return ji(e)?e:e[Yn]}function Uv(t){return(t[_e]&4)===4}function Jl(t){return(t[_e]&128)===128}function $v(t){return Tn(t[_t])}function yn(t,n){return n==null?null:t[n]}function Lp(t){t[io]=0}function Vp(t){t[_e]&1024||(t[_e]|=1024,Jl(t)&&co(t))}function Gv(t,n){for(;t>0;)n=n[ha],t--;return n}function As(t){return!!(t[_e]&9216||t[gn]?.dirty)}function ed(t){t[Kn].changeDetectionScheduler?.notify(8),t[_e]&64&&(t[_e]|=1024),As(t)&&co(t)}function co(t){t[Kn].changeDetectionScheduler?.notify(0);let n=Li(t);for(;n!==null&&!(n[_e]&8192||(n[_e]|=8192,!Jl(n)));)n=Li(n)}function td(t,n){if(so(t))throw new $(911,!1);t[Pi]===null&&(t[Pi]=[]),t[Pi].push(n)}function Wv(t,n){if(t[Pi]===null)return;let e=t[Pi].indexOf(n);e!==-1&&t[Pi].splice(e,1)}function Li(t){let n=t[_t];return Tn(n)?n[_t]:n}function Bp(t){return t[ir]??=[]}function jp(t){return t.cleanup??=[]}function qv(t,n,e,i){let a=Bp(n);a.push(e),t.firstCreatePass&&jp(t).push(i,a.length-1)}var Ee={lFrame:iy(null),bindingsEnabled:!0,skipHydrationRootTNode:null};var hp=!1;function Yv(){return Ee.lFrame.elementDepthCount}function Kv(){Ee.lFrame.elementDepthCount++}function zp(){Ee.lFrame.elementDepthCount--}function nd(){return Ee.bindingsEnabled}function Hp(){return Ee.skipHydrationRootTNode!==null}function Up(t){return Ee.skipHydrationRootTNode===t}function $p(){Ee.skipHydrationRootTNode=null}function de(){return Ee.lFrame.lView}function Qe(){return Ee.lFrame.tView}function A(t){return Ee.lFrame.contextLView=t,t[gt]}function O(t){return Ee.lFrame.contextLView=null,t}function Dt(){let t=Gp();for(;t!==null&&t.type===64;)t=t.parent;return t}function Gp(){return Ee.lFrame.currentTNode}function Qv(){let t=Ee.lFrame,n=t.currentTNode;return t.isParent?n:n.parent}function lo(t,n){let e=Ee.lFrame;e.currentTNode=t,e.isParent=n}function Wp(){return Ee.lFrame.isParent}function qp(){Ee.lFrame.isParent=!1}function Yp(){return Ee.lFrame.contextLView}function Kp(){return hp}function ys(t){let n=hp;return hp=t,n}function id(){let t=Ee.lFrame,n=t.bindingRootIndex;return n===-1&&(n=t.bindingRootIndex=t.tView.bindingStartIndex),n}function Os(){return Ee.lFrame.bindingIndex}function Xv(t){return Ee.lFrame.bindingIndex=t}function ga(){return Ee.lFrame.bindingIndex++}function uo(t){let n=Ee.lFrame,e=n.bindingIndex;return n.bindingIndex=n.bindingIndex+t,e}function Zv(){return Ee.lFrame.inI18n}function Jv(t,n){let e=Ee.lFrame;e.bindingIndex=e.bindingRootIndex=t,ad(n)}function ey(){return Ee.lFrame.currentDirectiveIndex}function ad(t){Ee.lFrame.currentDirectiveIndex=t}function ty(t){let n=Ee.lFrame.currentDirectiveIndex;return n===-1?null:t[n]}function od(){return Ee.lFrame.currentQueryIndex}function Rs(t){Ee.lFrame.currentQueryIndex=t}function tD(t){let n=t[fe];return n.type===2?n.declTNode:n.type===1?t[Ht]:null}function Qp(t,n,e){if(e&4){let a=n,o=t;for(;a=a.parent,a===null&&!(e&1);)if(a=tD(o),a===null||(o=o[ha],a.type&10))break;if(a===null)return!1;n=a,t=o}let i=Ee.lFrame=ny();return i.currentTNode=n,i.lView=t,!0}function rd(t){let n=ny(),e=t[fe];Ee.lFrame=n,n.currentTNode=e.firstChild,n.lView=t,n.tView=e,n.contextLView=t,n.bindingIndex=e.bindingStartIndex,n.inI18n=!1}function ny(){let t=Ee.lFrame,n=t===null?null:t.child;return n===null?iy(t):n}function iy(t){let n={currentTNode:null,isParent:!0,lView:null,tView:null,selectedIndex:-1,contextLView:null,elementDepthCount:0,currentNamespace:null,currentDirectiveIndex:-1,bindingRootIndex:-1,bindingIndex:-1,currentQueryIndex:0,parent:t,child:null,inI18n:!1};return t!==null&&(t.child=n),n}function ay(){let t=Ee.lFrame;return Ee.lFrame=t.parent,t.currentTNode=null,t.lView=null,t}var Xp=ay;function sd(){let t=ay();t.isParent=!0,t.tView=null,t.selectedIndex=-1,t.contextLView=null,t.elementDepthCount=0,t.currentDirectiveIndex=-1,t.currentNamespace=null,t.bindingRootIndex=-1,t.bindingIndex=-1,t.currentQueryIndex=0}function oy(t){return(Ee.lFrame.contextLView=Gv(t,Ee.lFrame.contextLView))[gt]}function dn(){return Ee.lFrame.selectedIndex}function ba(t){Ee.lFrame.selectedIndex=t}function cr(){let t=Ee.lFrame;return Zl(t.tView,t.selectedIndex)}function $t(){Ee.lFrame.currentNamespace=Pp}function va(){nD()}function nD(){Ee.lFrame.currentNamespace=null}function Zp(){return Ee.lFrame.currentNamespace}var ry=!0;function cd(){return ry}function Ps(t){ry=t}function fp(t,n=null,e=null,i){let a=Jp(t,n,e,i);return a.resolveInjectorInitializers(),a}function Jp(t,n=null,e=null,i,a=new Set){return new Ja([e||Yt,Pv(t)],n||tr(),null,a)}var re=class t{static THROW_IF_NOT_FOUND=Qa;static NULL=new Jo;static create(n,e){if(Array.isArray(n))return fp({name:``},e,n,``);{let i=n.name??``;return fp({name:i},n.parent,n.providers,i)}}static ɵprov=he({token:t,providedIn:`any`,factory:()=>Se(Ql)});static __NG_ELEMENT_ID__=-1};var ce=new D(``);var Ne=class{static __NG_ELEMENT_ID__=iD;static __NG_ENV_ID__=n=>n};var Vl=class extends Ne{_lView;constructor(n){super(),this._lView=n}get destroyed(){return so(this._lView)}onDestroy(n){let e=this._lView;return td(e,n),()=>Wv(e,n)}};function iD(){return new Vl(de())}var sy=!1;var cy=new D(``);var mo=(()=>{class t{taskId=0;pendingTasks=new Set;destroyed=!1;pendingTask=new hi(!1);debugTaskTracker=f(cy,{optional:!0});get hasPendingTasks(){return this.destroyed?!1:this.pendingTask.value}get hasPendingTasksObservable(){return this.destroyed?new xe(e=>{e.next(!1),e.complete()}):this.pendingTask}add(){!this.hasPendingTasks&&!this.destroyed&&this.pendingTask.next(!0);let e=this.taskId++;return this.pendingTasks.add(e),this.debugTaskTracker?.add(e),e}has(e){return this.pendingTasks.has(e)}remove(e){this.pendingTasks.delete(e),this.debugTaskTracker?.remove(e),this.pendingTasks.size===0&&this.hasPendingTasks&&this.pendingTask.next(!1)}ngOnDestroy(){this.pendingTasks.clear(),this.hasPendingTasks&&this.pendingTask.next(!1),this.destroyed=!0,this.pendingTask.unsubscribe()}static ɵprov=he({token:t,providedIn:`root`,factory:()=>new t})}return t})();var gp=class extends L{__isAsync;destroyRef=void 0;pendingTasks=void 0;constructor(n=!1){super(),this.__isAsync=n,Bv()&&(this.destroyRef=f(Ne,{optional:!0})??void 0,this.pendingTasks=f(mo,{optional:!0})??void 0)}emit(n){let e=pe(null);try{super.next(n)}finally{pe(e)}}subscribe(n,e,i){let a=n,o=e||(()=>null),r=i;if(n&&typeof n==`object`){let c=n;a=c.next?.bind(c),o=c.error?.bind(c),r=c.complete?.bind(c)}this.__isAsync&&(o=this.wrapInTimeout(o),a&&(a=this.wrapInTimeout(a)),r&&(r=this.wrapInTimeout(r)));let s=super.subscribe({next:a,error:o,complete:r});return n instanceof De&&n.add(s),s}wrapInTimeout(n){return e=>{let i=this.pendingTasks?.add();setTimeout(()=>{try{n(e)}finally{i!==void 0&&this.pendingTasks?.remove(i)}})}}};var J=gp;function Bl(...t){}function eh(t){let n,e;function i(){t=Bl;try{e!==void 0&&typeof cancelAnimationFrame==`function`&&cancelAnimationFrame(e),n!==void 0&&clearTimeout(n)}catch{}}return n=setTimeout(()=>{t(),i()}),typeof requestAnimationFrame==`function`&&(e=requestAnimationFrame(()=>{t(),i()})),()=>i()}function ly(t){return queueMicrotask(()=>t()),()=>{t=Bl}}var th=`isAngularZone`;var _s=th+`_ID`;var aD=0;var K=class t{hasPendingMacrotasks=!1;hasPendingMicrotasks=!1;isStable=!0;onUnstable=new J(!1);onMicrotaskEmpty=new J(!1);onStable=new J(!1);onError=new J(!1);constructor(n){let{enableLongStackTrace:e=!1,shouldCoalesceEventChangeDetection:i=!1,shouldCoalesceRunChangeDetection:a=!1,scheduleInRootZone:o=sy}=n;if(typeof Zone>`u`)throw new $(908,!1);Zone.assertZonePatched();let r=this;r._nesting=0,r._outer=r._inner=Zone.current,Zone.TaskTrackingZoneSpec&&(r._inner=r._inner.fork(new Zone.TaskTrackingZoneSpec)),e&&Zone.longStackTraceZoneSpec&&(r._inner=r._inner.fork(Zone.longStackTraceZoneSpec)),r.shouldCoalesceEventChangeDetection=!a&&i,r.shouldCoalesceRunChangeDetection=a,r.callbackScheduled=!1,r.scheduleInRootZone=o,sD(r)}static isInAngularZone(){return typeof Zone<`u`&&Zone.current.get(th)===!0}static assertInAngularZone(){if(!t.isInAngularZone())throw new $(909,!1)}static assertNotInAngularZone(){if(t.isInAngularZone())throw new $(909,!1)}run(n,e,i){return this._inner.run(n,e,i)}runTask(n,e,i,a){let o=this._inner,r=o.scheduleEventTask(`NgZoneEvent: `+a,n,oD,Bl,Bl);try{return o.runTask(r,e,i)}finally{o.cancelTask(r)}}runGuarded(n,e,i){return this._inner.runGuarded(n,e,i)}runOutsideAngular(n){return this._outer.run(n)}};var oD={};function nh(t){if(t._nesting==0&&!t.hasPendingMicrotasks&&!t.isStable)try{t._nesting++,t.onMicrotaskEmpty.emit(null)}finally{if(t._nesting--,!t.hasPendingMicrotasks)try{t.runOutsideAngular(()=>t.onStable.emit(null))}finally{t.isStable=!0}}}function rD(t){if(t.isCheckStableRunning||t.callbackScheduled)return;t.callbackScheduled=!0;function n(){eh(()=>{t.callbackScheduled=!1,bp(t),t.isCheckStableRunning=!0,nh(t),t.isCheckStableRunning=!1})}t.scheduleInRootZone?Zone.root.run(()=>{n()}):t._outer.run(()=>{n()}),bp(t)}function sD(t){let n=()=>{rD(t)},e=aD++;t._inner=t._inner.fork({name:`angular`,properties:{[th]:!0,[_s]:e,[_s+e]:!0},onInvokeTask:(i,a,o,r,s,c)=>{if(cD(c))return i.invokeTask(o,r,s,c);try{return Cv(t),i.invokeTask(o,r,s,c)}finally{(t.shouldCoalesceEventChangeDetection&&r.type===`eventTask`||t.shouldCoalesceRunChangeDetection)&&n(),Sv(t)}},onInvoke:(i,a,o,r,s,c,m)=>{try{return Cv(t),i.invoke(o,r,s,c,m)}finally{t.shouldCoalesceRunChangeDetection&&!t.callbackScheduled&&!lD(c)&&n(),Sv(t)}},onHasTask:(i,a,o,r)=>{i.hasTask(o,r),a===o&&(r.change==`microTask`?(t._hasPendingMicrotasks=r.microTask,bp(t),nh(t)):r.change==`macroTask`&&(t.hasPendingMacrotasks=r.macroTask))},onHandleError:(i,a,o,r)=>(i.handleError(o,r),t.runOutsideAngular(()=>t.onError.emit(r)),!1)})}function bp(t){t._hasPendingMicrotasks||(t.shouldCoalesceEventChangeDetection||t.shouldCoalesceRunChangeDetection)&&t.callbackScheduled===!0?t.hasPendingMicrotasks=!0:t.hasPendingMicrotasks=!1}function Cv(t){t._nesting++,t.isStable&&(t.isStable=!1,t.onUnstable.emit(null))}function Sv(t){t._nesting--,nh(t)}var xs=class{hasPendingMicrotasks=!1;hasPendingMacrotasks=!1;isStable=!0;onUnstable=new J;onMicrotaskEmpty=new J;onStable=new J;onError=new J;run(n,e,i){return n.apply(e,i)}runGuarded(n,e,i){return n.apply(e,i)}runOutsideAngular(n){return n()}runTask(n,e,i,a){return n.apply(e,i)}};function cD(t){return dy(t,`__ignore_ng_zone__`)}function lD(t){return dy(t,`__scheduler_tick__`)}function dy(t,n){return!Array.isArray(t)||t.length!==1?!1:t[0]?.data?.[n]===!0}var Mn=class{_console=console;handleError(n){this._console.error(`ERROR`,n)}};function uy(t){return my(t)?t:new vp(t)}function my(t){return t instanceof Error||typeof t==`object`&&t!==null&&typeof t.name==`string`&&typeof t.message==`string`}var vp=class extends Error{constructor(n){super(String(n),{cause:n}),this.name=`ErrorBoundaryWrappedError`}};var zi=new D(``,{factory:()=>{let t=f(K),n=f(Et),e;return i=>{t.runOutsideAngular(()=>{n.destroyed&&!e?setTimeout(()=>{throw i}):(e??=n.get(Mn),e.handleError(i))})}}});var py={provide:to,useValue:()=>{f(Mn,{optional:!0})},multi:!0};var dD=new D(``,{factory:()=>{let t=f(ce).defaultView;if(!t)return;let n=f(zi),e=o=>{n(o.reason),o.preventDefault()},i=o=>{o.error?n(o.error):n(new Error(o.message,{cause:o})),o.preventDefault()},a=()=>{t.addEventListener(`unhandledrejection`,e),t.addEventListener(`error`,i)};typeof Zone<`u`?Zone.root.run(a):a(),f(Ne).onDestroy(()=>{t.removeEventListener(`error`,i),t.removeEventListener(`unhandledrejection`,e)})}});function ih(){return an([Rv(()=>{f(dD)})])}function M(t,n){let[e,i,a]=Hm(t,n?.equal),o=e;o[yt];return o.set=i,o.update=a,o.asReadonly=ld.bind(o),o}function ld(){let t=this[yt];if(t.readonlyFn===void 0){let n=()=>this();n[yt]=t,t.readonlyFn=n}return t.readonlyFn}var Hi=new D(``,{factory:()=>uD});var uD=`ng`;var dd=new D(``);var Ui=new D(``,{providedIn:`platform`,factory:()=>`unknown`});var Fs=new D(``);var ya=new D(``,{factory:()=>f(ce).body?.querySelector(`[ngCspNonce]`)?.getAttribute(`ngCspNonce`)||null});var lr=(()=>{class t{view;node;constructor(e,i){this.view=e,this.node=i}static __NG_ELEMENT_ID__=mD}return t})();function mD(){return new lr(de(),Dt())}var gi=class{};var Ls=new D(``,{factory:()=>!0});var ah=new D(``);var ud=(()=>{class t{static ɵprov=he({token:t,providedIn:`root`,factory:()=>new yp})}return t})();var yp=class{dirtyEffectCount=0;queues=new Map;add(n){this.enqueue(n),this.schedule(n)}schedule(n){n.dirty&&this.dirtyEffectCount++}remove(n){let e=n.zone,i=this.queues.get(e);i.has(n)&&(i.delete(n),n.dirty&&this.dirtyEffectCount--)}enqueue(n){let e=n.zone;this.queues.has(e)||this.queues.set(e,new Set);let i=this.queues.get(e);i.has(n)||i.add(n)}flush(){for(;this.dirtyEffectCount>0;){let n=!1;for(let[e,i]of this.queues)e===null?n||=this.flushQueue(i):n||=e.run(()=>this.flushQueue(i));n||(this.dirtyEffectCount=0)}}flushQueue(n){let e=!1;for(let i of n)i.dirty&&(this.dirtyEffectCount--,e=!0,i.run());return e}};var jl=class{[yt];constructor(n){this[yt]=n}destroy(){this[yt].destroy()}};function Pt(t,n){let e=n?.injector??f(re),i=n?.manualCleanup!==!0?e.get(Ne):null,a,o=e.get(lr,null,{optional:!0}),r=e.get(gi);return o!==null?(a=fy(o.view,r,t),i instanceof Vl&&i._lView===o.view&&(i=null)):a=fD(t,e.get(ud),r),a.injector=e,i!==null&&(a.onDestroyFns=[i.onDestroy(()=>a.destroy())]),new jl(a)}var hy=s(r({},$m),{cleanupFns:void 0,zone:null,onDestroyFns:null,run(){let t=ys(!1);try{Gm(this)}finally{ys(t)}},cleanup(){if(!this.cleanupFns?.length)return;let t=pe(null);try{for(;this.cleanupFns.length;)this.cleanupFns.pop()()}finally{this.cleanupFns=[],pe(t)}}});var pD=s(r({},hy),{consumerMarkedDirty(){this.scheduler.schedule(this),this.notifier.notify(12)},destroy(){if(la(this),this.onDestroyFns!==null)for(let t of this.onDestroyFns)t();this.cleanup(),this.scheduler.remove(this)}});var hD=s(r({},hy),{consumerMarkedDirty(){this.view[_e]|=8192,co(this.view),this.notifier.notify(13)},destroy(){if(la(this),this.onDestroyFns!==null)for(let t of this.onDestroyFns)t();this.cleanup(),this.view[Fi]?.delete(this)}});function fy(t,n,e){let i=Object.create(hD);return i.view=t,i.zone=typeof Zone<`u`?Zone.current:null,i.notifier=n,i.userFn=e,i.fn=gy(i,e),t[Fi]??=new Set,t[Fi].add(i),i.consumerMarkedDirty(i),i}function fD(t,n,e){let i=Object.create(pD);return i.userFn=t,i.fn=gy(i,t),i.scheduler=n,i.notifier=e,i.zone=typeof Zone<`u`?Zone.current:null,i.scheduler.add(i),i.notifier.notify(12),i}function gy(t,n){return()=>{n(e=>(t.cleanupFns??=[]).push(e))}}function on(t){return typeof t==`function`&&t[yt]!==void 0}var _a=(()=>{class t{internalPendingTasks=f(mo);scheduler=f(gi);errorHandler=f(zi);add(){let e=this.internalPendingTasks.add();return()=>{this.internalPendingTasks.has(e)&&(this.scheduler.notify(11),this.internalPendingTasks.remove(e))}}run(e){let i=this.add();try{e().catch(this.errorHandler).finally(i)}catch(a){this.errorHandler(a),i()}}static ɵprov=he({token:t,providedIn:`root`,factory:()=>new t})}return t})();var _r=Symbol(`InputSignalNode#UNSET`);var Jh=s(r({},Uo),{transformFn:void 0,applyValueToInputSignal(t,n){da(t,n)}});function qs(t){return{toString:t}.toString()}var Be=(function(t){return t[t.TemplateCreateStart=0]=`TemplateCreateStart`,t[t.TemplateCreateEnd=1]=`TemplateCreateEnd`,t[t.TemplateUpdateStart=2]=`TemplateUpdateStart`,t[t.TemplateUpdateEnd=3]=`TemplateUpdateEnd`,t[t.LifecycleHookStart=4]=`LifecycleHookStart`,t[t.LifecycleHookEnd=5]=`LifecycleHookEnd`,t[t.OutputStart=6]=`OutputStart`,t[t.OutputEnd=7]=`OutputEnd`,t[t.BootstrapApplicationStart=8]=`BootstrapApplicationStart`,t[t.BootstrapApplicationEnd=9]=`BootstrapApplicationEnd`,t[t.BootstrapComponentStart=10]=`BootstrapComponentStart`,t[t.BootstrapComponentEnd=11]=`BootstrapComponentEnd`,t[t.ChangeDetectionStart=12]=`ChangeDetectionStart`,t[t.ChangeDetectionEnd=13]=`ChangeDetectionEnd`,t[t.ChangeDetectionSyncStart=14]=`ChangeDetectionSyncStart`,t[t.ChangeDetectionSyncEnd=15]=`ChangeDetectionSyncEnd`,t[t.AfterRenderHooksStart=16]=`AfterRenderHooksStart`,t[t.AfterRenderHooksEnd=17]=`AfterRenderHooksEnd`,t[t.ComponentStart=18]=`ComponentStart`,t[t.ComponentEnd=19]=`ComponentEnd`,t[t.DeferBlockStateStart=20]=`DeferBlockStateStart`,t[t.DeferBlockStateEnd=21]=`DeferBlockStateEnd`,t[t.DynamicComponentStart=22]=`DynamicComponentStart`,t[t.DynamicComponentEnd=23]=`DynamicComponentEnd`,t[t.HostBindingsUpdateStart=24]=`HostBindingsUpdateStart`,t[t.HostBindingsUpdateEnd=25]=`HostBindingsUpdateEnd`,t})(Be||{});var yd=class{previousValue;currentValue;firstChange;constructor(n,e,i){this.previousValue=n,this.currentValue=e,this.firstChange=i}isFirstChange(){return this.firstChange}};function a_(t,n,e,i){n!==null?n.applyValueToInputSignal(n,i):t[e]=i}var o_=null;var ft=(()=>{o_=by;let t=()=>by;return t.ngInherit=!0,t})();function DD(){return o_}function by(t){return t.type.prototype.ngOnChanges&&(t.setInput=ID),kD}function kD(){let t=r_(this),n=t?.current;if(n){let e=t.previous;if(e===pa)t.previous=n;else for(let i in n)e[i]=n[i];t.current=null,this.ngOnChanges(n)}}function ID(t,n,e,i,a){let o=this.declaredInputs[i],r=r_(t)||MD(t,{previous:pa,current:null}),s=r.current||(r.current={}),c=r.previous,m=c[o];s[o]=new yd(m&&m.currentValue,e,c===pa),a_(t,n,a,e)}var gh=`__ngSimpleChanges__`;function r_(t){return Object.hasOwn(t,gh)&&t[gh]||null}function MD(t,n){return t[gh]=n}var vy=[];var Ye=function(t,n=null,e){for(let i=0;i<vy.length;i++){let a=vy[i];a(t,n,e)}};function ND(t,n,e){let{ngOnChanges:i,ngOnInit:a,ngDoCheck:o}=n.type.prototype;if(i){let r=DD()(n);(e.preOrderHooks??=[]).push(t,r),(e.preOrderCheckHooks??=[]).push(t,r)}a&&(e.preOrderHooks??=[]).push(0-t,a),o&&((e.preOrderHooks??=[]).push(t,o),(e.preOrderCheckHooks??=[]).push(t,o))}function s_(t,n){for(let e=n.directiveStart,i=n.directiveEnd;e<i;e++){let{ngAfterContentInit:r,ngAfterContentChecked:s,ngAfterViewInit:c,ngAfterViewChecked:m,ngOnDestroy:u}=t.data[e].type.prototype;r&&(t.contentHooks??=[]).push(-e,r),s&&((t.contentHooks??=[]).push(e,s),(t.contentCheckHooks??=[]).push(e,s)),c&&(t.viewHooks??=[]).push(-e,c),m&&((t.viewHooks??=[]).push(e,m),(t.viewCheckHooks??=[]).push(e,m)),u!=null&&(t.destroyHooks??=[]).push(e,u)}}function fd(t,n,e){c_(t,n,3,e)}function gd(t,n,e,i){(t[_e]&3)===e&&c_(t,n,e,i)}function oh(t,n){let e=t[_e];(e&3)===n&&(e&=16383,e+=1,t[_e]=e)}function c_(t,n,e,i){let a=i!==void 0?t[io]&65535:0,o=i??-1,r=n.length-1,s=0;for(let c=a;c<r;c++)if(typeof n[c+1]==`number`){if(s=n[c],i!=null&&s>=i)break}else n[c]<0&&(t[io]+=65536),(s<o||o==-1)&&(TD(t,e,n,c),t[io]=(t[io]&4294901760)+c+2),c++}function yy(t,n){Ye(Be.LifecycleHookStart,t,n);let e=pe(null);try{n.call(t)}finally{pe(e),Ye(Be.LifecycleHookEnd,t,n)}}function TD(t,n,e,i){let a=e[i]<0,o=e[i+1],s=t[a?-e[i]:e[i]];a?t[_e]>>14<t[io]>>16&&(t[_e]&3)===n&&(t[_e]+=16384,yy(s,o)):yy(s,o)}var mr=-1;var fo=class{factory;name;injectImpl;resolving=!1;canSeeViewProviders;multi;componentProviders;index;providerFactory;constructor(n,e,i,a){this.factory=n,this.name=a,this.canSeeViewProviders=e,this.injectImpl=i}};function AD(t){return(t.flags&8)!==0}function OD(t){return(t.flags&16)!==0}function RD(t,n,e){let i=0;for(;i<e.length;){let a=e[i];if(typeof a==`number`){if(a!==0)break;i++;let o=e[i++],r=e[i++],s=e[i++];t.setAttribute(n,r,s,o)}else{let o=a,r=e[++i];PD(o)?t.setProperty(n,o,r):t.setAttribute(n,o,r),i++}}return i}function l_(t){return t===3||t===4||t===6}function PD(t){return t.charCodeAt(0)===64}function pr(t,n){if(!(n===null||n.length===0))if(t===null||t.length===0)t=n.slice();else{let e=-1;for(let i=0;i<n.length;i++){let a=n[i];typeof a==`number`?e=a:e===0||(e===-1||e===2?_y(t,e,a,null,n[++i]):_y(t,e,a,null,null))}}return t}function _y(t,n,e,i,a){let o=0,r=t.length;if(n===-1)r=-1;else for(;o<t.length;){let s=t[o++];if(typeof s==`number`){if(s===n){r=-1;break}else if(s>n){r=o-1;break}}}for(;o<t.length;){let s=t[o];if(typeof s==`number`)break;if(s===e){a!==null&&(t[o+1]=a);return}o++,a!==null&&o++}r!==-1&&(t.splice(r,0,n),o=r+1),t.splice(o++,0,e),a!==null&&t.splice(o++,0,a)}function d_(t){return t!==mr}function _d(t){return t&32767}function FD(t){return t>>16}function xd(t,n){let e=FD(t),i=n;for(;e>0;)i=i[ha],e--;return i}var bh=!0;function Cd(t){let n=bh;return bh=t,n}var u_=255;var m_=5;var VD=0;var _i={};function BD(t,n,e){let i;typeof e==`string`?i=e.charCodeAt(0)||0:Object.hasOwn(e,eo)&&(i=e[eo]),i??=e[eo]=VD++;let a=i&u_,o=1<<a;n.data[t+(a>>m_)]|=o}function Sd(t,n){let e=p_(t,n);if(e!==-1)return e;let i=n[fe];i.firstCreatePass&&(t.injectorIndex=n.length,rh(i.data,t),rh(n,null),rh(i.blueprint,null));let a=ef(t,n),o=t.injectorIndex;if(d_(a)){let r=_d(a),s=xd(a,n),c=s[fe].data;for(let m=0;m<8;m++)n[o+m]=s[r+m]|c[r+m]}return n[o+8]=a,o}function rh(t,n){t.push(0,0,0,0,0,0,0,0,n)}function p_(t,n){return t.injectorIndex===-1||t.parent&&t.parent.injectorIndex===t.injectorIndex||n[t.injectorIndex+8]===null?-1:t.injectorIndex}function ef(t,n){if(t.parent&&t.parent.injectorIndex!==-1)return t.parent.injectorIndex;let e=0,i=null,a=n;for(;a!==null;){if(i=v_(a),i===null)return mr;if(e++,a=a[ha],i.injectorIndex!==-1)return i.injectorIndex|e<<16}return mr}function vh(t,n,e){BD(t,n,e)}function jD(t,n){if(n===`class`)return t.classes;if(n===`style`)return t.styles;let e=t.attrs;if(e){let i=e.length,a=0;for(;a<i;){let o=e[a];if(l_(o))break;if(o===0)a=a+2;else if(typeof o==`number`)for(a++;a<i&&typeof e[a]==`string`;)a++;else{if(o===n)return e[a+1];a=a+2}}}return null}function h_(t,n,e){if(e&8||t!==void 0)return t;Wl(n,`NodeInjector`)}function f_(t,n,e,i){if(e&8&&i===void 0&&(i=null),(e&3)===0){let a=t[Bi],o=nn(void 0);try{return a?a.get(n,i,e&8):Ep(n,i,e&8)}finally{nn(o)}}return h_(i,n,e)}function g_(t,n,e,i=0,a){if(t!==null){if(n[_e]&2048&&!(i&2)){let r=$D(t,n,e,i,_i);if(r!==_i)return r}let o=b_(t,n,e,i,_i);if(o!==_i)return o}return f_(n,e,i,a)}function b_(t,n,e,i,a){let o=HD(e);if(typeof o==`function`){if(!Qp(n,t,i))return i&1?h_(a,e,i):f_(n,e,i,a);try{let r;if(r=o(i),r==null&&!(i&8))Wl(e);else return r}finally{Xp()}}else if(typeof o==`number`){let r=null,s=p_(t,n),c=mr,m=i&1?n[Kt][Ht]:null;for((s===-1||i&4)&&(c=s===-1?ef(t,n):n[s+8],c===mr||!Cy(i,!1)?s=-1:(r=n[fe],s=_d(c),n=xd(c,n)));s!==-1;){let u=n[fe];if(xy(o,s,u.data)){let g=zD(s,n,e,r,i,m);if(g!==_i)return g}c=n[s+8],c!==mr&&Cy(i,n[fe].data[s+8]===m)&&xy(o,s,n)?(r=u,s=_d(c),n=xd(c,n)):s=-1}}return a}function zD(t,n,e,i,a,o){let r=n[fe],s=r.data[t+8],u=bd(s,r,e,i==null?yi(s)&&bh:i!=r&&(s.type&3)!==0,a&1&&o===s);return u!==null?zs(n,r,u,s,a):_i}function bd(t,n,e,i,a){let o=t.providerIndexes,r=n.data,s=o&1048575,c=t.directiveStart,m=t.directiveEnd,u=o>>20,g=i?s:s+u,v=a?s+u:m;for(let b=g;b<v;b++){let y=r[b];if(b<c&&e===y||b>=c&&y.type===e)return b}if(a){let b=r[c];if(b&&Qn(b)&&b.type===e)return c}return null}function zs(t,n,e,i,a){let o=t[e],r=n.data;if(o instanceof fo){let s=o;if(s.resolving)throw wp(``);let c=Cd(s.canSeeViewProviders);s.resolving=!0;r[e].type||r[e];let g=s.injectImpl?nn(s.injectImpl):null;Qp(t,i,0);try{o=t[e]=s.factory(void 0,a,r,t,i),n.firstCreatePass&&e>=i.directiveStart&&ND(e,r[e],n)}finally{g!==null&&nn(g),Cd(c),s.resolving=!1,Xp()}}return o}function HD(t){if(typeof t==`string`)return t.charCodeAt(0)||0;let n=Object.hasOwn(t,eo)?t[eo]:void 0;return typeof n==`number`?n>=0?n&u_:UD:n}function xy(t,n,e){let i=1<<t;return!!(e[n+(t>>m_)]&i)}function Cy(t,n){return!(t&2)&&!(t&1&&n)}var $i=class{_tNode;_lView;constructor(n,e){this._tNode=n,this._lView=e}get(n,e,i){return g_(this._tNode,this._lView,n,Xa(i),e)}};function UD(){return new $i(Dt(),de())}function It(t){return qs(()=>{let n=t.prototype.constructor,e=n[vs]||yh(n),i=Object.prototype,a=Object.getPrototypeOf(t.prototype).constructor;for(;a&&a!==i;){let o=a[vs]||yh(a);if(o&&o!==e)return o;a=Object.getPrototypeOf(a)}return o=>new o})}function yh(t){return _p(t)?()=>{let n=yh(Ot(t));return n&&n()}:ua(t)}function $D(t,n,e,i,a){let o=t,r=n;for(;o!==null&&r!==null&&r[_e]&2048&&!sr(r);){let s=b_(o,r,e,i|2,_i);if(s!==_i)return s;i&=-5;let c=o.parent;if(!c){let m=r[Ap];if(m){let u=m.get(e,_i,i);if(u!==_i)return u}c=v_(r),r=r[ha]}o=c}return a}function v_(t){let n=t[fe],e=n.type;return e===2?n.declTNode:e===1?t[Ht]:null}function tf(t){return jD(Dt(),t)}function se(t){return{token:t.token,providedIn:t.autoProvided===!1?null:`root`,factory:t.factory,value:void 0}}function GD(){return xr(Dt(),de())}function xr(t,n){return new G(vn(t,n))}var G=(()=>{class t{nativeElement;constructor(e){this.nativeElement=e}static __NG_ELEMENT_ID__=GD}return t})();function y_(t){return t instanceof G?t.nativeElement:t}function WD(){return this._results[Symbol.iterator]()}var _n=class{_emitDistinctChangesOnly;dirty=!0;_onDirty=void 0;_results=[];_changesDetected=!1;_changes=void 0;length=0;first=void 0;last=void 0;get changes(){return this._changes??=new L}constructor(n=!1){this._emitDistinctChangesOnly=n}get(n){return this._results[n]}map(n){return this._results.map(n)}filter(n){return this._results.filter(n)}find(n){return this._results.find(n)}reduce(n,e){return this._results.reduce(n,e)}forEach(n){this._results.forEach(n)}some(n){return this._results.some(n)}toArray(){return this._results.slice()}toString(){return this._results.toString()}reset(n,e){this.dirty=!1;let i=Tv(n);(this._changesDetected=!Nv(this._results,i,e))&&(this._results=i,this.length=i.length,this.last=i[this.length-1],this.first=i[0])}notifyOnChanges(){this._changes!==void 0&&(this._changesDetected||!this._emitDistinctChangesOnly)&&this._changes.next(this)}onDirty(n){this._onDirty=n}setDirty(){this.dirty=!0,this._onDirty?.()}destroy(){this._changes!==void 0&&(this._changes.complete(),this._changes.unsubscribe())}[Symbol.iterator]=WD};function __(t){return(t.flags&128)===128}var nf=(function(t){return t[t.OnPush=0]=`OnPush`,t[t.Eager=1]=`Eager`,t[t.Default=1]=`Default`,t})(nf||{});var x_=new Map;var qD=0;function YD(){return qD++}function KD(t){x_.set(t[Is],t)}function _h(t){x_.delete(t[Is])}var Sy=`__ngContext__`;function hr(t,n){ji(n)?(t[Sy]=n[Is],KD(n)):t[Sy]=n}function C_(t){return w_(t[ar])}function S_(t){return w_(t[Nn])}function w_(t){for(;t!==null&&!Tn(t);)t=t[Nn];return t}var xh;function af(t){xh=t}function E_(){if(xh!==void 0)return xh;if(typeof document<`u`)return document;throw new $(210,!1)}var D_=`r`;var k_=`di`;var I_=!1;var M_=new D(``,{factory:()=>I_});var wy=new WeakMap;function QD(t,n){if(t==null||typeof t!=`object`)return;let e=wy.get(t);e||(e=new WeakSet,wy.set(t,e)),e.add(n)}function Pd(t){return(t.flags&32)===32}var JD=()=>null;function N_(t,n,e=!1){return JD(t,n,e)}function T_(t,n){let e=t.contentQueries;if(e!==null){let i=pe(null);try{for(let a=0;a<e.length;a+=2){let o=e[a],r=e[a+1];if(r!==-1){let s=t.data[r];Rs(o),s.contentQueries(2,n[r],r)}}}finally{pe(i)}}}function Ch(t,n,e){Rs(0);let i=pe(null);try{n(t,e)}finally{pe(i)}}function of(t,n,e){if(Rp(n)){let i=pe(null);try{let a=n.directiveStart,o=n.directiveEnd;for(let r=a;r<o;r++){let s=t.data[r];if(s.contentQueries){let c=e[r];s.contentQueries(1,c,r)}}}finally{pe(i)}}}var Jn=(function(t){return t[t.Emulated=0]=`Emulated`,t[t.None=2]=`None`,t[t.ShadowDom=3]=`ShadowDom`,t[t.ExperimentalIsolatedShadowDom=4]=`ExperimentalIsolatedShadowDom`,t})(Jn||{});var md;function ek(){if(md===void 0&&(md=null,bi.trustedTypes))try{md=bi.trustedTypes.createPolicy(`angular`,{createHTML:t=>t,createScript:t=>t,createScriptURL:t=>t})}catch{}return md}function Fd(t){return ek()?.createHTML(t)||t}var Gi=class{changingThisBreaksApplicationSecurity;constructor(n){this.changingThisBreaksApplicationSecurity=n}toString(){return`SafeValue must use [property]=binding: ${this.changingThisBreaksApplicationSecurity} (see ${zl})`}};var Sh=class extends Gi{getTypeName(){return`HTML`}};var wh=class extends Gi{getTypeName(){return`Style`}};var Eh=class extends Gi{getTypeName(){return`Script`}};var Dh=class extends Gi{getTypeName(){return`URL`}};var kh=class extends Gi{getTypeName(){return`ResourceURL`}};function ei(t){return t instanceof Gi?t.changingThisBreaksApplicationSecurity:t}function Ca(t,n){let e=A_(t);if(e!=null&&e!==n){if(e===`ResourceURL`&&n===`URL`)return!0;throw new Error(`Required a safe ${n}, got a ${e} (see ${zl})`)}return e===n}function A_(t){return t instanceof Gi&&t.getTypeName()||null}function rf(t){return new Sh(t)}function sf(t){return new wh(t)}function cf(t){return new Eh(t)}function lf(t){return new Dh(t)}function df(t){return new kh(t)}function tk(t){let n=new Mh(t);return nk()?new Ih(n):n}var Ih=class{inertDocumentHelper;constructor(n){this.inertDocumentHelper=n}getInertBodyElement(n){n=`<body><remove></remove>`+n;try{let e=new window.DOMParser().parseFromString(Fd(n),`text/html`).body;return e===null?this.inertDocumentHelper.getInertBodyElement(n):(e.firstChild?.remove(),e)}catch{return null}}};var Mh=class{defaultDoc;inertDocument;constructor(n){this.defaultDoc=n,this.inertDocument=this.defaultDoc.implementation.createHTMLDocument(`sanitization-inert`)}getInertBodyElement(n){let e=this.inertDocument.createElement(`template`);return e.innerHTML=Fd(n),e}};function nk(){try{return!!new window.DOMParser().parseFromString(Fd(``),`text/html`)}catch{return!1}}var ik=/^(?!javascript:)(?:[a-z0-9+.-]+:|[^&:\/?#]*(?:[\/?#]|$))/i;function Ys(t){return t=String(t),t.match(ik)?t:`unsafe:`+t}function Wi(t){let n=Object.create(null);for(let e of t.split(`,`))n[e]=!0;return n}function Ks(...t){let n=Object.create(null);for(let e of t)for(let i in e)Object.hasOwn(e,i)&&(n[i]=!0);return n}var O_=Wi(`area,br,col,hr,img,wbr`);var R_=Wi(`colgroup,dd,dt,li,p,tbody,td,tfoot,th,thead,tr`);var P_=Wi(`rp,rt`);var ak=Ks(P_,R_);var Ey=Ks(O_,Ks(R_,Wi(`address,article,aside,blockquote,caption,center,del,details,dialog,dir,div,dl,figure,figcaption,footer,h1,h2,h3,h4,h5,h6,header,hgroup,hr,ins,main,map,menu,nav,ol,pre,section,summary,table,ul`)),Ks(P_,Wi(`a,abbr,acronym,audio,b,bdi,bdo,big,br,cite,code,del,dfn,em,font,i,img,ins,kbd,label,map,mark,picture,q,ruby,rp,rt,s,samp,small,source,span,strike,strong,sub,sup,time,track,tt,u,var,video`)),ak);var F_=Wi(`background,cite,href,itemtype,longdesc,poster,src,xlink:href`);var lk=Ks(F_,Wi(`abbr,accesskey,align,alt,autoplay,axis,bgcolor,border,cellpadding,cellspacing,class,clear,color,cols,colspan,compact,controls,coords,datetime,default,dir,download,face,headers,height,hidden,hreflang,hspace,ismap,itemscope,itemprop,kind,label,lang,language,loop,media,muted,nohref,nowrap,open,preload,rel,rev,role,rows,rowspan,rules,scope,scrolling,shape,size,sizes,span,srclang,srcset,start,summary,tabindex,target,title,translate,type,usemap,valign,value,vspace,width`),Wi(`aria-activedescendant,aria-atomic,aria-autocomplete,aria-busy,aria-checked,aria-colcount,aria-colindex,aria-colspan,aria-controls,aria-current,aria-describedby,aria-details,aria-disabled,aria-dropeffect,aria-errormessage,aria-expanded,aria-flowto,aria-grabbed,aria-haspopup,aria-hidden,aria-invalid,aria-keyshortcuts,aria-label,aria-labelledby,aria-level,aria-live,aria-modal,aria-multiline,aria-multiselectable,aria-orientation,aria-owns,aria-placeholder,aria-posinset,aria-pressed,aria-readonly,aria-relevant,aria-required,aria-roledescription,aria-rowcount,aria-rowindex,aria-rowspan,aria-selected,aria-setsize,aria-sort,aria-valuemax,aria-valuemin,aria-valuenow,aria-valuetext`));var dk=Wi(`script,style,template`);var Nh=class{sanitizedSomething=!1;buf=[];sanitizeChildren(n){let e=n.firstChild,i=!0,a=[];for(;e;){if(e.nodeType===Node.ELEMENT_NODE?i=this.startElement(e):e.nodeType===Node.TEXT_NODE?this.chars(e.nodeValue):this.sanitizedSomething=!0,i&&e.firstChild){a.push(e),e=pk(e);continue}for(;e;){e.nodeType===Node.ELEMENT_NODE&&this.endElement(e);let o=mk(e);if(o){e=o;break}e=a.pop()}}return this.buf.join(``)}startElement(n){let e=Dy(n).toLowerCase();if(!Object.hasOwn(Ey,e))return this.sanitizedSomething=!0,!Object.hasOwn(dk,e);this.buf.push(`<`),this.buf.push(e);let i=n.attributes;for(let a=0;a<i.length;a++){let o=i.item(a),r=o.name,s=r.toLowerCase();if(!Object.hasOwn(lk,s)){this.sanitizedSomething=!0;continue}let c=o.value;F_[s]&&(c=Ys(c)),this.buf.push(` `,r,`="`,ky(c),`"`)}return this.buf.push(`>`),!0}endElement(n){let e=Dy(n).toLowerCase();Object.hasOwn(Ey,e)&&!Object.hasOwn(O_,e)&&(this.buf.push(`</`),this.buf.push(e),this.buf.push(`>`))}chars(n){this.buf.push(ky(n))}};function uk(t,n){return(t.compareDocumentPosition(n)&Node.DOCUMENT_POSITION_CONTAINED_BY)!==Node.DOCUMENT_POSITION_CONTAINED_BY}function mk(t){let n=t.nextSibling;if(n&&t!==n.previousSibling)throw L_(n);return n}function pk(t){let n=t.firstChild;if(n&&uk(t,n))throw L_(n);return n}function Dy(t){let n=t.nodeName;return typeof n==`string`?n:`FORM`}function L_(t){return new Error(`Failed to sanitize html because the element is clobbered: ${t.outerHTML}`)}var hk=/[\uD800-\uDBFF][\uDC00-\uDFFF]/g;var fk=/([^\#-~ |!])/g;function ky(t){return t.replace(/&/g,`&amp;`).replace(hk,function(n){let e=n.charCodeAt(0),i=n.charCodeAt(1);return`&#`+((e-55296)*1024+(i-56320)+65536)+`;`}).replace(fk,function(n){return`&#`+n.charCodeAt(0)+`;`}).replace(/</g,`&lt;`).replace(/>/g,`&gt;`)}var pd;function uf(t,n){let e=null;try{pd=pd||tk(t);let i=n?String(n):``;e=pd.getInertBodyElement(i);let a=5,o=i;do{if(a===0)throw new Error(`Failed to sanitize html because the input is unstable`);a--,i=o,o=e.innerHTML,e=pd.getInertBodyElement(i)}while(i!==o);return Fd(new Nh().sanitizeChildren(Iy(e)||e))}finally{if(e){let i=Iy(e)||e;for(;i.firstChild;)i.firstChild.remove()}}}function Iy(t){return`content`in t&&gk(t)?t.content:null}function gk(t){return t.nodeType===Node.ELEMENT_NODE&&t.nodeName===`TEMPLATE`}var bk=/^>|^->|<!--|-->|--!>|<!-$/g;var vk=/(<|>)/g;var yk=`​$1​`;function _k(t){return t.replace(bk,n=>n.replace(vk,yk))}function xk(t,n){return t.createText(n)}function Ck(t,n,e){t.setValue(n,e)}function Sk(t,n){return t.createComment(_k(n))}function V_(t,n,e){return t.createElement(n,e)}function po(t,n,e,i,a){t.insertBefore(n,e,i,a)}function B_(t,n,e){t.appendChild(n,e)}function My(t,n,e,i,a){i!==null?po(t,n,e,i,a):B_(t,n,e)}function j_(t,n,e,i){t.removeChild(null,n,e,i)}function wk(t,n,e){t.setAttribute(n,`style`,e)}function Ek(t,n,e){e===``?t.removeAttribute(n,`class`):t.setAttribute(n,`class`,e)}function z_(t,n,e){let{mergedAttrs:i,classes:a,styles:o}=e;i!==null&&RD(t,n,i),a!==null&&Ek(t,n,a),o!==null&&wk(t,n,o)}function Cn(t){let n=Dk();return n?n.sanitize(bn.URL,t)||``:Ca(t,`URL`)?ei(t):Ys(Rt(t))}function Dk(){let t=de();return t&&t[Kn].sanitizer}function kk(t){return t instanceof Function?t():t}function Ik(t,n,e){let i=t.length;for(;;){let a=t.indexOf(n,e);if(a===-1)return a;if(a===0||t.charCodeAt(a-1)<=32){let o=n.length;if(a+o===i||t.charCodeAt(a+o)<=32)return a}e=a+1}}var H_=`ng-template`;function Mk(t,n,e,i){let a=0;if(i){for(;a<n.length&&typeof n[a]==`string`;a+=2)if(n[a]===`class`&&Ik(n[a+1].toLowerCase(),e,0)!==-1)return!0}else if(mf(t))return!1;if(a=n.indexOf(1,a),a>-1){let o;for(;++a<n.length&&typeof(o=n[a])==`string`;)if(o.toLowerCase()===e)return!0}return!1}function mf(t){return t.type===4&&t.value!==H_}function Nk(t,n,e){return n===(t.type===4&&!e?H_:t.value)}function Tk(t,n,e){let i=4,a=t.attrs,o=a!==null?Rk(a):0,r=!1;for(let s=0;s<n.length;s++){let c=n[s];if(typeof c==`number`){if(!r&&!Xn(i)&&!Xn(c))return!1;if(r&&Xn(c))continue;r=!1,i=c|i&1;continue}if(!r)if(i&4){if(i=2|i&1,c!==``&&!Nk(t,c,e)||c===``&&n.length===1){if(Xn(i))return!1;r=!0}}else if(i&8){if(a===null||!Mk(t,a,c,e)){if(Xn(i))return!1;r=!0}}else{let m=n[++s],u=Ak(c,a,mf(t),e);if(u===-1){if(Xn(i))return!1;r=!0;continue}if(m!==``){let g;if(u>o?g=``:g=a[u+1].toLowerCase(),i&2&&m!==g){if(Xn(i))return!1;r=!0}}}}return Xn(i)||r}function Xn(t){return(t&1)===0}function Ak(t,n,e,i){if(n===null)return-1;let a=0;if(i||!e){let o=!1;for(;a<n.length;){let r=n[a];if(r===t)return a;if(r===3||r===6)o=!0;else if(r===1||r===2){let s=n[++a];for(;typeof s==`string`;)s=n[++a];continue}else{if(r===4)break;if(r===0){a+=4;continue}}a+=o?1:2}return-1}else return Pk(n,t)}function U_(t,n,e=!1){for(let i=0;i<n.length;i++)if(Tk(t,n[i],e))return!0;return!1}function Ok(t){let n=t.attrs;if(n!=null){let e=n.indexOf(5);if((e&1)===0)return n[e+1]}return null}function Rk(t){for(let n=0;n<t.length;n++){let e=t[n];if(l_(e))return n}return t.length}function Pk(t,n){let e=t.indexOf(4);if(e>-1)for(e++;e<t.length;){let i=t[e];if(typeof i==`number`)return-1;if(i===n)return e;e++}return-1}function Fk(t,n){e:for(let e=0;e<n.length;e++){let i=n[e];if(t.length===i.length){for(let a=0;a<t.length;a++)if(t[a]!==i[a])continue e;return!0}}return!1}function Ny(t,n){return t?`:not(`+n.trim()+`)`:n}function Lk(t){let n=t[0],e=1,i=2,a=``,o=!1;for(;e<t.length;){let r=t[e];if(typeof r==`string`)if(i&2){let s=t[++e];a+=`[`+r+(s.length>0?`="`+s+`"`:``)+`]`}else i&8?a+=`.`+r:i&4&&(a+=` `+r);else a!==``&&!Xn(r)&&(n+=Ny(o,a),a=``),i=r,o=o||!Xn(i);e++}return a!==``&&(n+=Ny(o,a)),n}function Vk(t){return t.map(Lk).join(`,`)}function Bk(t){let n=[],e=[],i=1,a=2;for(;i<t.length;){let o=t[i];if(typeof o==`string`)a===2?o!==``&&n.push(o,t[++i]):a===8&&e.push(o);else{if(!Xn(a))break;a=o}i++}return e.length&&n.push(1,...e),n}var xt={};var xi=(function(t){return t[t.Important=1]=`Important`,t[t.DashCase=2]=`DashCase`,t})(xi||{});var jk;function pf(t,n){return jk(t,n)}var hf=new Set;typeof document<`u`&&document?.documentElement?.getAnimations;var Th=new WeakMap;function $_(t){return t?t[ha]??t:null}var dr=new WeakSet;function zk(t,n,e){let i=Th.get(t);if(!i||i.length===0)return;let a=n.parentNode,o=n.previousSibling,r=$_(e);for(let s=i.length-1;s>=0;s--){let{el:c,declarationView:m}=i[s],u=c.parentNode;c===n?(i.splice(s,1),dr.add(c),c.dispatchEvent(new CustomEvent(`animationend`,{detail:{cancel:!0}}))):o&&c===o?(i.splice(s,1),c.dispatchEvent(new CustomEvent(`animationend`,{detail:{cancel:!0}})),c.parentNode?.removeChild(c)):u&&a&&u!==a&&(r===null||m===null||r===m)&&(i.splice(s,1),c.dispatchEvent(new CustomEvent(`animationend`,{detail:{cancel:!0}})),c.parentNode?.removeChild(c))}}function Hk(t,n,e){let i=$_(e),a=Th.get(t);a?a.some(o=>o.el===n)||a.push({el:n,declarationView:i}):Th.set(t,[{el:n,declarationView:i}])}var Ld=(function(t){return t[t.CHANGE_DETECTION=0]=`CHANGE_DETECTION`,t[t.AFTER_NEXT_RENDER=1]=`AFTER_NEXT_RENDER`,t})(Ld||{});var ni=new D(``);var Ty=new Set;function Ci(t){Ty.has(t)||(Ty.add(t),performance?.mark?.(`mark_feature_usage`,{detail:{feature:t}}))}var Vd=(()=>{class t{impl=null;execute(){this.impl?.execute()}static ɵprov=he({token:t,providedIn:`root`,factory:()=>new t})}return t})();var ff=[0,1,2,3];var gf=(()=>{class t{ngZone=f(K);scheduler=f(gi);errorHandler=f(Mn,{optional:!0});sequences=new Set;deferredRegistrations=new Set;executing=!1;constructor(){f(ni,{optional:!0})}execute(){let e=this.sequences.size>0;e&&Ye(Be.AfterRenderHooksStart),this.executing=!0;for(let i of ff)for(let a of this.sequences)if(!(a.erroredOrDestroyed||!a.hooks[i]))try{a.pipelinedValue=this.ngZone.runOutsideAngular(()=>this.maybeTrace(()=>{let o=a.hooks[i];return o(a.pipelinedValue)},a.snapshot))}catch(o){a.erroredOrDestroyed=!0,this.errorHandler?.handleError(o)}this.executing=!1;for(let i of this.sequences)i.afterRun(),i.once&&(this.sequences.delete(i),i.destroy());for(let i of this.deferredRegistrations)this.sequences.add(i);this.deferredRegistrations.size>0&&this.scheduler.notify(7),this.deferredRegistrations.clear(),e&&Ye(Be.AfterRenderHooksEnd)}register(e){let{view:i}=e;i!==void 0?((i[ao]??=[]).push(e),co(i),i[_e]|=8192):this.executing?this.deferredRegistrations.add(e):this.addSequence(e)}addSequence(e){this.sequences.add(e),this.scheduler.notify(7)}unregister(e){this.executing&&this.sequences.has(e)?(e.erroredOrDestroyed=!0,e.pipelinedValue=void 0,e.once=!0):(this.sequences.delete(e),this.deferredRegistrations.delete(e))}maybeTrace(e,i){return i?i.run(Ld.AFTER_NEXT_RENDER,e):e()}static ɵprov=he({token:t,providedIn:`root`,factory:()=>new t})}return t})();var Hs=class{impl;hooks;view;once;snapshot;erroredOrDestroyed=!1;pipelinedValue=void 0;unregisterOnDestroy;constructor(n,e,i,a,o,r=null){this.impl=n,this.hooks=e,this.view=i,this.once=a,this.snapshot=r,this.unregisterOnDestroy=o?.onDestroy(()=>this.destroy())}afterRun(){this.erroredOrDestroyed=!1,this.pipelinedValue=void 0,this.snapshot?.dispose(),this.snapshot=null}destroy(){this.impl.unregister(this),this.unregisterOnDestroy?.();let n=this.view?.[ao];n&&(this.view[ao]=n.filter(e=>e!==this))}};function Mt(t,n){let e=n?.injector??f(re);return Ci(`NgAfterNextRender`),$k(t,e,n,!0)}function Uk(t){return t instanceof Function?[void 0,void 0,t,void 0]:[t.earlyRead,t.write,t.mixedReadWrite,t.read]}function $k(t,n,e,i){let a=n.get(Vd);a.impl??=n.get(gf);let o=n.get(ni,null,{optional:!0}),r=e?.manualCleanup!==!0?n.get(Ne):null,s=n.get(lr,null,{optional:!0}),c=new Hs(a.impl,Uk(t),s?.view,i,r,o?.snapshot(null));return a.impl.register(c),c}var Ah=null;var G_=null;function Ay(t,n,e,i){Ah?.maybeQueueEnterAnimation(t,n,e,i)}function Oy(t,n,e,i){Ah===null?i(!1):Ah.runLeaveAnimationsWithCallback(t,n,e,i)}function Gk(t){G_?.initViewDetachAnimations(t)}function Wk(t){G_?.clearViewDetachAnimations(t)}function ur(t,n,e,i,a,o,r,s){if(a!=null){let c,m=!1;Tn(a)?c=a:ji(a)&&(m=!0,a=a[Yn]);let u=Ut(a);t===0&&i!==null?(Ay(s,i,o,e),r==null?B_(n,i,u):po(n,i,u,r||null,!0)):t===1&&i!==null?(po(n,i,u,r||null,!0),zk(o,u,s),dr.has(u)||Ay(s,i,o,e)):t===2?(s?.[jv]?.leave?.has(o.index)&&Hk(o,u,s),dr.delete(u),Oy(s,o,e,g=>{if(dr.has(u)){dr.delete(u);return}j_(n,u,m,g)})):t===3&&(dr.delete(u),Oy(s,o,e,()=>{n.destroyNode(u)})),c!=null&&iI(n,t,e,c,o,i,r)}}function qk(t,n){W_(t,n),n[Yn]=null,n[Ht]=null}function Yk(t,n,e,i,a,o){i[Yn]=a,i[Ht]=n,jd(t,i,e,1,a,o)}function W_(t,n){n[Kn].changeDetectionScheduler?.notify(9),jd(t,n,n[Je],2,null,null)}function Kk(t){let n=t[ar];if(!n)return sh(t[fe],t);for(;n;){let e=null;if(ji(n))e=n[ar];else{let i=n[bt];i&&(e=i)}if(!e){for(;n&&!n[Nn]&&n!==t;)ji(n)&&sh(n[fe],n),n=n[_t];n===null&&(n=t),ji(n)&&sh(n[fe],n),e=n&&n[Nn]}n=e}}function bf(t,n){let e=t[ro],i=e.indexOf(n);e.splice(i,1)}function Bd(t,n){if(so(n))return;let e=n[Je];e.destroyNode&&jd(t,n,e,3,null,null),Kk(n)}function sh(t,n){if(so(n))return;let e=pe(null);try{n[_e]&=-129,n[_e]|=256,n[gn]&&la(n[gn]),Xk(t,n),Qk(t,n),n[fe].type===1&&n[Je].destroy();let i=n[fa];if(i!==null&&Tn(n[_t])){i!==n[_t]&&bf(i,n);let a=n[vi];a!==null&&a.detachView(t)}_h(n)}finally{pe(e)}}function Qk(t,n){let e=t.cleanup,i=n[ir];if(e!==null)for(let r=0;r<e.length-1;r+=2)if(typeof e[r]==`string`){let s=e[r+3];s>=0?i[s]():i[-s].unsubscribe(),r+=2}else{let s=i[e[r+1]];e[r].call(s)}i!==null&&(n[ir]=null);let a=n[Pi];if(a!==null){n[Pi]=null;for(let r=0;r<a.length;r++){let s=a[r];s()}}let o=n[Fi];if(o!==null){n[Fi]=null;for(let r of o)r.destroy()}}function Xk(t,n){let e;if(t!=null&&(e=t.destroyHooks)!=null)for(let i=0;i<e.length;i+=2){let a=n[e[i]];if(!(a instanceof fo)){let o=e[i+1];if(Array.isArray(o))for(let r=0;r<o.length;r+=2){let s=a[o[r]],c=o[r+1];Ye(Be.LifecycleHookStart,s,c);try{c.call(s)}finally{Ye(Be.LifecycleHookEnd,s,c)}}else{Ye(Be.LifecycleHookStart,a,o);try{o.call(a)}finally{Ye(Be.LifecycleHookEnd,a,o)}}}}}function q_(t,n,e){if(n===null)throw new $(510,!1);return Zk(t,n.parent,e)}function Zk(t,n,e){let i=n;for(;i!==null&&i.type&168;)n=i,i=n.parent;if(i===null)return e[Yn];if(yi(i)){let{encapsulation:a}=t.data[i.directiveStart+i.componentOffset];if(a===Jn.None||a===Jn.Emulated)return null}return vn(i,e)}function Y_(t,n,e){return eI(t,n,e)}function Jk(t,n,e){return t.type&40?vn(t,e):null}var eI=Jk;function vf(t,n,e,i){let a=q_(t,i,n),o=n[Je],s=Y_(i.parent||n[Ht],i,n);if(a!=null)if(Array.isArray(e))for(let c=0;c<e.length;c++)My(o,a,e[c],s,!1);else My(o,a,e,s,!1)}function Bs(t,n){if(n!==null){let e=n.type;if(e&3)return vn(n,t);if(e&4)return Oh(-1,t[n.index]);if(e&8){let i=n.child;if(i!==null)return Bs(t,i);{let a=t[n.index];return Tn(a)?Oh(-1,a):Ut(a)}}else{if(e&128)return Bs(t,n.next);if(e&32)return pf(n,t)()||Ut(t[n.index]);{let i=K_(t,n);if(i!==null){if(Array.isArray(i))return i[0];return Bs(Li(t[Kt]),i)}else return Bs(t,n.next)}}}return null}function K_(t,n){if(n!==null){let i=t[Kt][Ht],a=n.projection;return i.projection[a]}return null}function Oh(t,n){let e=bt+t+1;if(e<n.length){let i=n[e],a=i[fe].firstChild;if(a!==null)return Bs(i,a)}return n[oo]}function yf(t,n,e,i,a,o,r){for(;e!=null;){let s=i[Bi];if(e.type===128){e=e.next;continue}let c=i[e.index],m=e.type;if(r&&n===0&&(c&&hr(Ut(c),i),e.flags|=2),!Pd(e))if(m&8)yf(t,n,e.child,i,a,o,!1),ur(n,t,s,a,c,e,o,i);else if(m&32){let u=pf(e,i),g;for(;g=u();)ur(n,t,s,a,g,e,o,i);ur(n,t,s,a,c,e,o,i)}else m&16?Q_(t,n,i,e,a,o):ur(n,t,s,a,c,e,o,i);e=r?e.projectionNext:e.next}}function jd(t,n,e,i,a,o){t.type===3?tI(e,i,n,a,o):yf(e,i,t.firstChild,n,a,o,!1)}function tI(t,n,e,i,a){let r=e[fe].firstChild,s=r.next,c=Ut(e[r.index]),m=Ut(e[s.index]),u=s.index+1,g=e[u];if(n===1||n===0)i!==null&&(g&&g.hasChildNodes()?po(t,i,g,a,!0):(po(t,i,c,a,!0),po(t,i,m,a,!0)));else if(n===2){if(g||(g=document.createDocumentFragment(),e[u]=g),c&&c.parentNode===g)return;let v=c;for(;v!==null;){let b=v.nextSibling;if(g.appendChild(v),v===m)break;v=b}}}function nI(t,n,e){let i=n[Je];Q_(i,0,n,e,q_(t,e,n),Y_(e.parent||n[Ht],e,n))}function Q_(t,n,e,i,a,o){let r=e[Kt],c=r[Ht].projection[i.projection];if(Array.isArray(c))for(let m=0;m<c.length;m++){let u=c[m];ur(n,t,e[Bi],a,u,i,o,e)}else{let m=c,u=r[_t];__(i)&&(m.flags|=128),yf(t,n,m,u,a,o,!0)}}function iI(t,n,e,i,a,o,r){let s=i[oo];if(s!==Ut(i)&&ur(n,t,e,o,s,a,r),(i[_e]&4)===0)for(let m=bt;m<i.length;m++){let u=i[m];jd(u[fe],u,t,n,o,s)}}function aI(t,n,e,i,a){if(n)a?t.addClass(e,i):t.removeClass(e,i);else{let o=i.indexOf(`-`)===-1?void 0:xi.DashCase;a==null?t.removeStyle(e,i,o):(typeof a==`string`&&a.endsWith(`!important`)&&(a=a.slice(0,-10),o|=xi.Important),t.setStyle(e,i,a,o))}}function _f(t,n,e,i,a,o,r,s,c,m,u){let g=it+i,v=g+a,b=oI(g,v),y=typeof m==`function`?m():m;return b[fe]={type:t,blueprint:b,template:e,queries:null,viewQuery:s,declTNode:n,data:b.slice().fill(null,g),bindingStartIndex:g,expandoStartIndex:v,hostBindingOpCodes:null,firstCreatePass:!0,firstUpdatePass:!0,staticViewQueries:!1,staticContentQueries:!1,preOrderHooks:null,preOrderCheckHooks:null,contentHooks:null,contentCheckHooks:null,viewHooks:null,viewCheckHooks:null,destroyHooks:null,cleanup:null,contentQueries:null,components:null,directiveRegistry:typeof o==`function`?o():o,pipeRegistry:typeof r==`function`?r():r,firstChild:null,schemas:c,consts:y,incompleteFirstPass:!1,ssrId:u}}function oI(t,n){let e=[];for(let i=0;i<n;i++)e.push(i<t?null:xt);return e}function rI(t){let n=t.tView;return n===null||n.incompleteFirstPass?t.tView=_f(1,null,t.template,t.decls,t.vars,t.directiveDefs,t.pipeDefs,t.viewQuery,t.schemas,t.consts,t.id):n}function xf(t,n,e,i,a,o,r,s,c,m,u){let g=n.blueprint.slice();return g[Yn]=a,g[_e]=i|1228,(m!==null||t&&t[_e]&2048)&&(g[_e]|=2048),Lp(g),g[_t]=g[ha]=t,g[gt]=e,g[Kn]=r||t&&t[Kn],g[Je]=s||t&&t[Je],g[Bi]=c||t&&t[Bi]||null,g[Ht]=o,g[Is]=YD(),g[no]=u,g[Ap]=m,g[Kt]=n.type==2?t[Kt]:g,g}function sI(t,n,e){let i=vn(n,t),a=rI(e),o=t[Kn].rendererFactory,r=Cf(t,xf(t,a,null,X_(e),i,n,null,o.createRenderer(i,e),null,null,null));return t[n.index]=r}function X_(t){let n=16;return t.signals?n=4096:t.onPush&&(n=64),n}function Z_(t,n,e,i){if(e===0)return-1;let a=n.length;for(let o=0;o<e;o++)n.push(i),t.blueprint.push(i),t.data.push(null);return a}function Cf(t,n){return t[ar]?t[Tp][Nn]=n:t[ar]=n,t[Tp]=n,n}function h(t=1){J_(Qe(),de(),dn()+t,!1)}function J_(t,n,e,i){if(!i)if((n[_e]&3)===3){let o=t.preOrderCheckHooks;o!==null&&fd(n,o,e)}else{let o=t.preOrderHooks;o!==null&&gd(n,o,0,e)}ba(e)}var Qs=(function(t){return t[t.None=0]=`None`,t[t.SignalBased=1]=`SignalBased`,t[t.HasDecoratorInputTransform=2]=`HasDecoratorInputTransform`,t})(Qs||{});function go(t,n,e,i){let a=pe(null);try{let[o,r,s]=t.inputs[e],c=null;(r&Qs.SignalBased)!==0&&(c=n[o][yt]),c!==null&&c.transformFn!==void 0?i=c.transformFn(i):s!==null&&(i=s.call(n,i)),t.setInput!==null?t.setInput(n,c,i,e,o):a_(n,c,o,i)}finally{pe(a)}}function e0(t,n,e,i,a){let o=dn(),r=i&2;try{ba(-1),r&&n.length>it&&J_(t,n,it,!1);Ye(r?Be.TemplateUpdateStart:Be.TemplateCreateStart,a,e),e(i,a)}finally{ba(o);Ye(r?Be.TemplateUpdateEnd:Be.TemplateCreateEnd,a,e)}}function zd(t,n,e){pI(t,n,e),(e.flags&64)===64&&hI(t,n,e)}function Xs(t,n,e=vn){let i=n.localNames;if(i!==null){let a=n.index+1;for(let o=0;o<i.length;o+=2){let r=i[o+1],s=r===-1?e(n,t):t[r];t[a++]=s}}}function cI(t,n,e,i){let o=i.get(M_,I_)||e===Jn.ShadowDom||e===Jn.ExperimentalIsolatedShadowDom;return t.selectRootElement(n,o)}function uI(t){return t===`class`?`className`:t===`for`?`htmlFor`:t===`formaction`?`formAction`:t===`innerHtml`?`innerHTML`:t===`readonly`?`readOnly`:t===`tabindex`?`tabIndex`:t}function mI(t,n,e,i,a,o){let r=n[fe];if(Hd(t,r,n,e,i)){yi(t)&&n0(n,t.index);return}t.type&3&&(e=uI(e)),t0(t,n,e,i,a,o)}function t0(t,n,e,i,a,o){if(t.type&3){let r=vn(t,n);i=o!=null?o(i,t.value||``,e):i,a.setProperty(r,e,i)}else t.type&12}function n0(t,n){let e=An(n,t);e[_e]&16||(e[_e]|=64)}function pI(t,n,e){let i=e.directiveStart,a=e.directiveEnd;yi(e)&&sI(n,e,t.data[i+e.componentOffset]),t.firstCreatePass||Sd(e,n);let o=e.initialInputs;for(let r=i;r<a;r++){let s=t.data[r],c=zs(n,t,r,e);if(hr(c,n),o!==null&&bI(n,r-i,c,s,e,o),Qn(s)){let m=An(e.index,n);m[gt]=zs(n,t,r,e)}}}function hI(t,n,e){let i=e.directiveStart,a=e.directiveEnd,o=e.index,r=ey();try{ba(o);for(let s=i;s<a;s++){let c=t.data[s],m=n[s];ad(s),(c.hostBindings!==null||c.hostVars!==0||c.hostAttrs!==null)&&fI(c,m)}}finally{ba(-1),ad(r)}}function fI(t,n){t.hostBindings!==null&&t.hostBindings(1,n)}function Sf(t,n){let e=t.directiveRegistry,i=null;if(e)for(let a=0;a<e.length;a++){let o=e[a];U_(n,o.selectors,!1)&&(i??=[],Qn(o)?i.unshift(o):i.push(o))}return i}function gI(t,n,e,i,a,o){let r=vn(t,n);i0(n[Je],r,o,t.value,e,i,a)}function i0(t,n,e,i,a,o,r){if(o==null)r?.(o,i||``,a),t.removeAttribute(n,a,e);else{let s=r==null?Rt(o):r(o,i||``,a);t.setAttribute(n,a,s,e)}}function bI(t,n,e,i,a,o){let r=o[n];if(r!==null)for(let s=0;s<r.length;s+=2){let c=r[s],m=r[s+1];go(i,e,c,m)}}function wf(t,n,e,i,a){let o=it+e,r=n[fe],s=a(r,n,t,i,e);n[o]=s,lo(t,!0);let c=t.type===2;return c?(z_(n[Je],s,t),(Yv()===0||rr(t))&&hr(s,n),Kv()):hr(s,n),cd()&&(!c||!Pd(t))&&vf(r,n,s,t),t}function Ef(t){let n=t;return Wp()?qp():(n=n.parent,lo(n,!1)),n}function vI(t,n){let e=t[Bi];if(!e)return;let i;try{i=e.get(zi,null)}catch{i=null}i?.(n)}function Hd(t,n,e,i,a){let o=t.inputs?.[i],r=t.hostDirectiveInputs?.[i],s=!1;if(r)for(let c=0;c<r.length;c+=2){let m=r[c],u=r[c+1],g=n.data[m];go(g,e[m],u,a),s=!0}if(o)for(let c of o){let m=e[c],u=n.data[c];go(u,m,i,a),s=!0}return s}function yI(t,n,e,i,a,o){let r=null,s=null,c=null,m=!1,u=t.directiveToIndex.get(i.type);if(typeof u==`number`?r=u:[r,s,c]=u,s!==null&&c!==null&&t.hostDirectiveInputs&&Object.hasOwn(t.hostDirectiveInputs,a)){let g=t.hostDirectiveInputs[a];for(let v=0;v<g.length;v+=2){let b=g[v];if(b>=s&&b<=c){let y=n.data[b],E=g[v+1];go(y,e[b],E,o),m=!0}else if(b>c)break}}return r!==null&&Object.hasOwn(i.inputs,a)&&(go(i,e[r],a,o),m=!0),m}function _I(t,n){let e=An(n,t),i=e[fe];xI(i,e);let a=e[Yn];a!==null&&e[no]===null&&(e[no]=N_(a,e[Bi])),Ye(Be.ComponentStart);try{Df(i,e,e[gt])}finally{Ye(Be.ComponentEnd,e[gt])}}function xI(t,n){for(let e=n.length;e<t.blueprint.length;e++)n.push(t.blueprint[e])}function Df(t,n,e){rd(n);try{let i=t.viewQuery;i!==null&&Ch(1,i,e);let a=t.template;a!==null&&e0(t,n,a,1,e),t.firstCreatePass&&(t.firstCreatePass=!1),n[vi]?.finishViewCreation(t),t.staticContentQueries&&T_(t,n),t.staticViewQueries&&Ch(2,t.viewQuery,e);let o=t.components;o!==null&&CI(n,o)}catch(i){throw t.firstCreatePass&&(t.incompleteFirstPass=!0,t.firstCreatePass=!1),i}finally{n[_e]&=-5,sd()}}function CI(t,n){for(let e=0;e<n.length;e++)_I(t,n[e])}function Zs(t,n,e,i){let a=pe(null);try{let o=n.tView,c=xf(t,o,e,t[_e]&4096?4096:16,null,n,null,null,i?.injector??null,i?.embeddedViewInjector??null,i?.dehydratedView??null);c[fa]=t[n.index];let u=t[vi];return u!==null&&(c[vi]=u.createEmbeddedView(o)),Df(o,c,e),c}finally{pe(a)}}function fr(t,n){return!n||n.firstChild===null||__(t)}function Us(t,n,e,i,a=!1){if(t.type===3){let o=t.firstChild,r=o.next,s=Ut(n[o.index]),c=Ut(n[r.index]),m=s;for(;m!==null&&(i.push(m),m!==c);)m=m.nextSibling;return i}for(;e!==null;){if(e.type===128){e=a?e.projectionNext:e.next;continue}let o=n[e.index];if(o!==null)if(Tn(o)){let s=o[oo];s!==o[Yn]&&i.push(Ut(o)),o[_e]&4||a0(o,i),i.push(s)}else i.push(Ut(o));let r=e.type;if(r&8)Us(t,n,e.child,i);else if(r&32){let s=pf(e,n),c;for(;c=s();)i.push(c)}else if(r&16){let s=K_(n,e);if(Array.isArray(s))i.push(...s);else{let c=Li(n[Kt]);Us(c[fe],c,s,i,!0)}}e=a?e.projectionNext:e.next}return i}function a0(t,n){for(let e=bt;e<t.length;e++){let i=t[e],a=i[fe].firstChild;a!==null&&Us(i[fe],i,a,n)}}function o0(t){if(t[ao]!==null){for(let n of t[ao])n.impl.addSequence(n);t[ao].length=0}}var r0=[];function SI(t){return t[gn]??wI(t)}function wI(t){let n=r0.pop()??Object.create(DI);return n.lView=t,n}function EI(t){t.lView[gn]!==t&&(t.lView=null,r0.push(t))}var DI=s(r({},Ha),{consumerIsAlwaysLive:!0,kind:`template`,consumerMarkedDirty:t=>{co(t.lView)},consumerOnSignalRead(){this.lView[gn]=this}});function kI(t){let n=t[gn]??Object.create(II);return n.lView=t,n}var II=s(r({},Ha),{consumerIsAlwaysLive:!0,kind:`template`,consumerMarkedDirty:t=>{let n=Li(t.lView);for(;n&&!s0(n[fe]);)n=Li(n);n&&Vp(n)},consumerOnSignalRead(){this.lView[gn]=this}});function s0(t){return t.type!==2}function c0(t){if(t[Fi]===null)return;let n=!0;for(;n;){let e=!1;for(let i of t[Fi])if(i.dirty&&(e=!0,i.zone===null||Zone.current===i.zone?i.run():i.zone.run(()=>i.run()),t[Fi]===null))return;n=e&&!!(t[_e]&8192)}}var MI=100;function l0(t,n=0){let i=t[Kn].rendererFactory;i.begin?.();try{NI(t,n)}finally{i.end?.()}}function NI(t,n){let e=Kp();try{ys(!0),Rh(t,n);let i=0;for(;As(t);){if(i===MI)throw new $(103,!1);i++,Rh(t,1)}}finally{ys(e)}}function TI(t,n,e,i){if(so(n))return;let a=n[_e];rd(n);let s=!0,c=null,m=null;s0(t)?(m=SI(n),c=ca(m)):tl()===null?(s=!1,m=kI(n),c=ca(m)):n[gn]&&(la(n[gn]),n[gn]=null);try{Lp(n),Xv(t.bindingStartIndex),e!==null&&e0(t,n,e,2,i);let u=(a&3)===3;if(u){let b=t.preOrderCheckHooks;b!==null&&fd(n,b,null)}else{let b=t.preOrderHooks;b!==null&&gd(n,b,0,null),oh(n,0)}if(AI(n),c0(n),d0(n,0),t.contentQueries!==null&&T_(t,n),true)if(u){let b=t.contentCheckHooks;b!==null&&fd(n,b)}else{let b=t.contentHooks;b!==null&&gd(n,b,1),oh(n,1)}RI(t,n);let g=t.components;g!==null&&m0(n,g,0);let v=t.viewQuery;if(v!==null&&Ch(2,v,i),true)if(u){let b=t.viewCheckHooks;b!==null&&fd(n,b)}else{let b=t.viewHooks;b!==null&&gd(n,b,2),oh(n,2)}if(t.firstUpdatePass===!0&&(t.firstUpdatePass=!1),n[Xl]){for(let b of n[Xl])b();n[Xl]=null}o0(n),n[_e]&=-73}catch(u){let g=!1,v=u,b=n;for(;b!==null;){if(Tn(b)){b=b[_t];continue}let y=b[or];if(y)try{let E=n[Kt][gt],oe={declarationInstance:E,declarationType:E?.constructor,caughtBy:y};y(uy(v),oe),g=!0;break}catch(E){v=E}b=b[_t]}if(!g)throw co(n),v}finally{m!==null&&(Ua(m,c),s&&EI(m)),sd()}}function d0(t,n){for(let e=C_(t);e!==null;e=S_(e))for(let i=bt;i<e.length;i++){let a=e[i];u0(a,n)}}function AI(t){for(let n=C_(t);n!==null;n=S_(n)){if(!(n[_e]&2))continue;let e=n[ro];for(let i=0;i<e.length;i++){let a=e[i];Vp(a)}}}function OI(t,n,e){Ye(Be.ComponentStart);let i=An(n,t);try{u0(i,e)}finally{Ye(Be.ComponentEnd,i[gt])}}function u0(t,n){Jl(t)&&Rh(t,n)}function Rh(t,n){let i=t[fe],a=t[_e],o=t[gn],r=!!(n===0&&a&16);if(r||=!!(a&64&&n===0),r||=!!(a&1024),r||=!!(o?.dirty&&zo(o)),r||=!1,o&&(o.dirty=!1),t[_e]&=-9217,r)TI(i,t,i.template,t[gt]);else if(a&8192){let s=pe(null);try{c0(t),d0(t,1);let c=i.components;c!==null&&m0(t,c,1),o0(t)}finally{pe(s)}}}function m0(t,n,e){for(let i=0;i<n.length;i++)OI(t,n[i],e)}function RI(t,n){let e=t.hostBindingOpCodes;if(e!==null)try{for(let i=0;i<e.length;i++){let a=e[i];if(a<0)ba(~a);else{let o=a,r=e[++i],s=e[++i];Jv(r,o);let c=n[o];Ye(Be.HostBindingsUpdateStart,c);try{s(2,c)}finally{Ye(Be.HostBindingsUpdateEnd,c)}}}}finally{ba(-1)}}function kf(t,n){let e=Kp()?64:1088;for(t[Kn].changeDetectionScheduler?.notify(n);t;){t[_e]|=e;let i=Li(t);if(sr(t)&&!i)return t;t=i}return null}function p0(t,n,e,i){return[t,!0,0,n,null,i,null,e,null,null]}function h0(t,n){let e=bt+n;if(e<t.length)return t[e]}function Js(t,n,e,i=!0){let a=n[fe];if(PI(a,n,t,e),i){let r=Oh(e,t),s=n[Je],c=s.parentNode(t[oo]);c!==null&&Yk(a,t[Ht],s,n,c,r)}let o=n[no];o!==null&&o.firstChild!==null&&(o.firstChild=null)}function f0(t,n){let e=$s(t,n);return e!==void 0&&Bd(e[fe],e),e}function $s(t,n){if(t.length<=bt)return;let e=bt+n,i=t[e];if(i){let a=i[fa];a!==null&&a!==t&&bf(a,i),n>0&&(t[e-1][Nn]=i[Nn]);let o=Ds(t,bt+n);qk(i[fe],i);let r=o[vi];r!==null&&r.detachView(o[fe]),i[_t]=null,i[Nn]=null,i[_e]&=-129}return i}function PI(t,n,e,i){let a=bt+i,o=e.length;i>0&&(e[a-1][Nn]=n),i<o-bt?(n[Nn]=e[a],Dp(e,bt+i,n)):(e.push(n),n[Nn]=null),n[_t]=e;let r=n[fa];r!==null&&e!==r&&g0(r,n);let s=n[vi];s!==null&&s.insertView(t),ed(n),n[_e]|=128}function g0(t,n){let e=t[ro],i=n[_t];if(ji(i))t[_e]|=2;else{let a=i[_t][Kt];n[Kt]!==a&&(t[_e]|=2)}e===null?t[ro]=[n]:e.push(n)}var xa=class{_lView;_cdRefInjectingView;_appRef=null;_attachedToViewContainer=!1;exhaustive;get rootNodes(){let n=this._lView,e=n[fe];return Us(e,n,e.firstChild,[])}constructor(n,e){this._lView=n,this._cdRefInjectingView=e}get context(){return this._lView[gt]}set context(n){this._lView[gt]=n}get destroyed(){return so(this._lView)}destroy(){if(this._appRef)this._appRef.detachView(this);else if(this._attachedToViewContainer){let n=this._lView[_t];if(Tn(n)){let e=n[Ms],i=e?e.indexOf(this):-1;i>-1&&($s(n,i),Ds(e,i))}this._attachedToViewContainer=!1}Bd(this._lView[fe],this._lView)}onDestroy(n){td(this._lView,n)}markForCheck(){kf(this._cdRefInjectingView||this._lView,4)}detach(){this._lView[_e]&=-129}reattach(){ed(this._lView),this._lView[_e]|=128}detectChanges(){this._lView[_e]|=1024,l0(this._lView)}checkNoChanges(){}attachToViewContainerRef(){if(this._appRef)throw new $(902,!1);this._attachedToViewContainer=!0}detachFromAppRef(){this._appRef=null;let n=sr(this._lView),e=this._lView[fa];e!==null&&!n&&bf(e,this._lView),W_(this._lView[fe],this._lView)}attachToAppRef(n){if(this._attachedToViewContainer)throw new $(902,!1);this._appRef=n;let e=sr(this._lView),i=this._lView[fa];i!==null&&!e&&g0(i,this._lView),ed(this._lView)}};var Ft=(()=>{class t{_declarationLView;_declarationTContainer;elementRef;static __NG_ELEMENT_ID__=FI;constructor(e,i,a){this._declarationLView=e,this._declarationTContainer=i,this.elementRef=a}get ssrId(){return this._declarationTContainer.tView?.ssrId||null}createEmbeddedView(e,i){return this.createEmbeddedViewImpl(e,i)}createEmbeddedViewImpl(e,i,a){return new xa(Zs(this._declarationLView,this._declarationTContainer,e,{embeddedViewInjector:i,dehydratedView:a}))}}return t})();function FI(){return Ud(Dt(),de())}function Ud(t,n){return t.type&4?new Ft(n,t,xr(t,n)):null}function vo(t,n,e,i,a){let o=t.data[n];if(o===null)o=LI(t,n,e,i,a),Zv()&&(o.flags|=32);else if(o.type&64){o.type=e,o.value=i,o.attrs=a;let r=Qv();o.injectorIndex=r===null?-1:r.injectorIndex}return lo(o,!0),o}function LI(t,n,e,i,a){let o=Gp(),r=Wp(),s=r?o:o&&o.parent,c=t.data[n]=BI(t,s,e,n,i,a);return VI(t,c,o,r),c}function VI(t,n,e,i){t.firstChild===null&&(t.firstChild=n),e!==null&&(i?e.child==null&&n.parent!==null&&(e.child=n):e.next===null&&(e.next=n,n.prev=e))}function BI(t,n,e,i,a,o){let r=n?n.injectorIndex:-1,s=0;return Hp()&&(s|=128),{type:e,index:i,insertBeforeIndex:null,injectorIndex:r,directiveStart:-1,directiveEnd:-1,directiveStylingLast:-1,componentOffset:-1,controlDirectiveIndex:-1,customControlIndex:-1,propertyBindings:null,flags:s,providerIndexes:0,value:a,namespace:Zp(),attrs:o,mergedAttrs:null,localNames:null,initialInputs:null,inputs:null,hostDirectiveInputs:null,outputs:null,hostDirectiveOutputs:null,directiveToIndex:null,tView:null,next:null,prev:null,projectionNext:null,child:null,parent:n,projection:null,styles:null,stylesWithoutHost:null,residualStyles:void 0,classes:null,classesWithoutHost:null,residualClasses:void 0,classBindings:0,styleBindings:0}}function jI(t){let n=t[Op]??[],i=t[_t][Je],a=[];for(let o of n)o.data[k_]!==void 0?a.push(o):zI(o,i);t[Op]=a}function zI(t,n){let e=0,i=t.firstChild;if(i){let a=t.data[D_];for(;e<a;){let o=i.nextSibling;j_(n,i,!1),i=o,e++}}}var HI=()=>null;var UI=()=>null;function wd(t,n){return HI(t,n)}function b0(t,n,e){return UI(t,n,e)}var v0=class{};var kt=class{};var Ae=class{destroyNode=null;static __NG_ELEMENT_ID__=()=>$I()};function $I(){let t=de(),e=An(Dt().index,t);return(ji(e)?e:t)[Je]}var y0=(()=>{class t{static ɵprov=he({token:t,providedIn:`root`,factory:()=>null})}return t})();function _0(t){return t.debugInfo?.className||t.type.name||null}var vd={};var Ed=class{injector;parentInjector;constructor(n,e){this.injector=n,this.parentInjector=e}get(n,e,i){let a=this.injector.get(n,vd,i);return a!==vd||e===vd?a:this.parentInjector.get(n,e,i)}};function If(t,n,e){return t[n]=e}function GI(t,n){return t[n]}function xn(t,n,e){if(e===xt)return!1;let i=t[n];return Object.is(i,e)?!1:(t[n]=e,!0)}function gr(t,n,e,i){let a=xn(t,n,e);return xn(t,n+1,i)||a}function WI(t,n,e,i,a){let o=gr(t,n,e,i);return xn(t,n+2,a)||o}function x0(t,n,e,i,a,o){let r=gr(t,n,e,i);return gr(t,n+2,a,o)||r}function ho(t,n,e){return function i(a){let o=i.__ngNativeEl__;o!==void 0&&QD(a,o);kf(yi(t)?An(t.index,n):n,5);let s=n[gt],c=Py(n,s,e,a),m=i.__ngNextListenerFn__;for(;m;)c=Py(n,s,m,a)&&c,m=m.__ngNextListenerFn__;return c}}function Py(t,n,e,i){let a=pe(null);try{return Ye(Be.OutputStart,n,e),e(i)!==!1}catch(o){return vI(t,o),!1}finally{Ye(Be.OutputEnd,n,e),pe(a)}}function Mf(t,n,e,i,a,o,r,s){let c=rr(t),m=!1,u=null;if(!i&&c&&(u=YI(n,e,o,t.index)),u!==null){let g=u.__ngLastListenerFn__||u;g.__ngNextListenerFn__=r,u.__ngLastListenerFn__=r,m=!0}else{let g=vn(t,e),v=i?i(g):g;i||(s.__ngNativeEl__=g);let b=a.listen(v,o,s);if(!qI(o))C0(i?E=>i(Ut(E[t.index])):t.index,n,e,o,s,b,!1)}return m}function qI(t){return t.startsWith(`animation`)||t.startsWith(`transition`)}function YI(t,n,e,i){let a=t.cleanup;if(a!=null)for(let o=0;o<a.length-1;o+=2){let r=a[o];if(r===e&&a[o+1]===i){let s=n[ir],c=a[o+2];return s&&s.length>c?s[c]:null}typeof r==`string`&&(o+=2)}return null}function C0(t,n,e,i,a,o,r){let s=n.firstCreatePass?jp(n):null,c=Bp(e),m=c.length;c.push(a,o),s&&s.push(i,t,m,(m+1)*(r?-1:1))}function Fy(t,n,e,i,a){let o=null,r=null,s=null,c=!1,m=t.directiveToIndex.get(e.type);if(typeof m==`number`?o=m:[o,r,s]=m,r!==null&&s!==null&&t.hostDirectiveOutputs&&Object.hasOwn(t.hostDirectiveOutputs,i)){let u=t.hostDirectiveOutputs[i];for(let g=0;g<u.length;g+=2){let v=u[g];if(v>=r&&v<=s)c=!0,Dd(t,n,v,u[g+1],i,a);else if(v>s)break}}return Object.hasOwn(e.outputs,i)&&(c=!0,Dd(t,n,o,i,i,a)),c}function Dd(t,n,e,i,a,o){let r=n[e],s=n[fe],g=r[s.data[e].outputs[i]].subscribe(o);C0(t.index,s,n,a,o,g,!0)}function Cr(){KI()}function KI(){let t=de(),n=Qe(),e=Dt();if(n.firstCreatePass&&XI(n,e),e.controlDirectiveIndex===-1)return;Ci(`NgSignalForms`);let i=t[e.controlDirectiveIndex];n.data[e.controlDirectiveIndex].controlDef.create(i,new kd(t,n,e))}function Sr(){QI()}function QI(){let t=de(),n=Qe(),e=cr();if(e.controlDirectiveIndex===-1)return;let i=n.data[e.controlDirectiveIndex].controlDef,a=t[e.controlDirectiveIndex];i.update(a,new kd(t,n,e))}var kd=class{lView;tView;tNode;hasPassThrough;constructor(n,e,i){this.lView=n,this.tView=e,this.tNode=i,this.hasPassThrough=!!(i.flags&4096)}get customControl(){return this.tNode.customControlIndex!==-1?this.lView[this.tNode.customControlIndex]:void 0}get nativeElement(){return vn(this.tNode,this.lView)}get descriptor(){return`<${this.tNode.value}>`}listenToCustomControlOutput(n,e){let i=this.tView.data[this.tNode.customControlIndex];Fy(this.tNode,this.lView,i,n,ho(this.tNode,this.lView,e))}listenToCustomControlModel(n){let e=this.tNode.flags&1024?`valueChange`:`checkedChange`,i=this.tView.data[this.tNode.customControlIndex];Fy(this.tNode,this.lView,i,e,ho(this.tNode,this.lView,n))}listenToDom(n,e){Mf(this.tNode,this.tView,this.lView,void 0,this.lView[Je],n,e,ho(this.tNode,this.lView,e))}setInputOnDirectives(n,e,i){let a=this.tNode.inputs?.[n],o=this.tNode.hostDirectiveInputs?.[n];if(!a&&!o)return!1;let r=!1;if(a)for(let s of a){if(s===this.tNode.controlDirectiveIndex)continue;let c=this.lView[s],m=this.tView.data[s];(!i||i(Vy(c,m,n)))&&(go(m,c,n,e),r=!0)}if(o)for(let s=0;s<o.length;s+=2){let c=o[s];if(c===this.tNode.controlDirectiveIndex)continue;let m=this.lView[c],u=o[s+1],g=this.tView.data[c];(!i||i(Vy(m,g,n)))&&(go(g,m,u,e),r=!0)}return r}setCustomControlModelInput(n){let e=this.tView.data[this.tNode.customControlIndex],i=this.tNode.flags&1024?`value`:`checked`;yI(this.tNode,this.tView,this.lView,e,i,n)}customControlHasInput(n){if(this.tNode.customControlIndex===-1)return!1;let e=this.tView.data[this.tNode.customControlIndex];return(e.signalFormsInputPresence??=this._buildCustomControlInputCache(e))[n]===!0}_buildCustomControlInputCache(n){let e={};for(let i in n.inputs)e[i]=!0;if(n.hostDirectives!==null){let i=[...n.hostDirectives];for(;i.length>0;){let a=i.shift();if(typeof a!=`function`){for(let r in a.inputs)e[a.inputs[r]]=!0;let o=Ly(a.directive);o!==null&&i.push(...o);continue}for(let o of a()){if(typeof o==`function`)continue;if(o.inputs)for(let s=0;s<o.inputs.length;s+=2){let c=o.inputs[s+1]||o.inputs[s];e[c]=!0}let r=Ly(o.directive);r!==null&&i.push(...r)}}}return e}};function Ly(t){return typeof t==`function`&&`ɵdir`in t?t.ɵdir.hostDirectives??null:null}function Vy(t,n,e){if(!n.inputs||!Object.hasOwn(n.inputs,e))return;let[i,a]=n.inputs[e];if((a&Qs.SignalBased)!==0){let r=t[i][yt];return r.value===_r?void 0:r.value}return t[i]}function XI(t,n,e){for(let a=n.directiveStart;a<n.directiveEnd;a++)if(t.data[a].controlDef){n.controlDirectiveIndex=a;break}if(n.controlDirectiveIndex===-1)return;let i=t.data[n.controlDirectiveIndex].controlDef;if(i.passThroughInput&&(n.inputs?.[i.passThroughInput]?.length??0)>1){n.flags|=4096;return}ZI(t,n)}function ZI(t,n){for(let e=n.directiveStart;e<n.directiveEnd;e++){let i=t.data[e];if(!(n.directiveToIndex&&!n.directiveToIndex.has(i.type))){if(By(i,`value`)){n.flags|=1024,n.customControlIndex=e;return}if(By(i,`checked`)){n.flags|=2048,n.customControlIndex=e;return}}}if(n.hostDirectiveInputs!==null&&n.hostDirectiveOutputs!==null&&n.directiveToIndex!==null){let e=(i,a)=>{let o=n.hostDirectiveInputs[i],r=n.hostDirectiveOutputs[i+`Change`];if(!o||!r)return!1;for(let s=0;s<o.length;s+=2){let c=o[s];for(let m=0;m<r.length;m+=2)if(c===r[m])for(let g of n.directiveToIndex.values()){if(!Array.isArray(g))continue;let[v,b,y]=g;if(c>=b&&c<=y)return n.flags|=a,n.customControlIndex=v,!0}}return!1};if(e(`value`,1024)||e(`checked`,2048))return}}function By(t,n){return JI(t,n)&&eM(t,n+`Change`)}function JI(t,n){return n in t.inputs}function eM(t,n){return n in t.outputs}var Ph=Symbol(`BINDING`);var yo=new D(``);function Id(t,n,e){let i=e?t.styles:null,a=e?t.classes:null,o=0;if(n!==null)for(let r=0;r<n.length;r++){let s=n[r];if(typeof s==`number`)o=s;else if(o==1)a=Hl(a,s);else if(o==2){let c=s,m=n[++r];i=Hl(i,c+`: `+m+`;`)}}e?t.styles=i:t.stylesWithoutHost=i,e?t.classes=a:t.classesWithoutHost=a}function je(t,n=0){let e=de();if(e===null)return Se(t,n);return g_(Dt(),e,Ot(t),n)}function Nf(){throw new Error(`invalid`)}function S0(t,n,e,i,a){let o=i===null?null:{"":-1},r=a(t,e);if(r!==null){let s=r,c=null,m=null;for(let u of r)if(u.resolveHostDirectives!==null){[s,c,m]=u.resolveHostDirectives(r);break}iM(t,n,e,s,o,c,m)}o!==null&&i!==null&&tM(e,i,o)}function tM(t,n,e){let i=t.localNames=[];for(let a=0;a<n.length;a+=2){let o=e[n[a+1]];if(o==null)throw new $(-301,!1);i.push(n[a],o)}}function nM(t,n,e){n.componentOffset=e,(t.components??=[]).push(n.index)}function iM(t,n,e,i,a,o,r){let s=i.length,c=null;for(let v=0;v<s;v++){let b=i[v];c===null&&Qn(b)&&(c=b,nM(t,e,v)),vh(Sd(e,n),t,b.type)}lM(e,t.data.length,s),c?.viewProvidersResolver&&c.viewProvidersResolver(c);for(let v=0;v<s;v++){let b=i[v];b.providersResolver&&b.providersResolver(b)}let m=!1,u=!1,g=Z_(t,n,s,null);s>0&&(e.directiveToIndex=new Map);for(let v=0;v<s;v++){let b=i[v];if(e.mergedAttrs=pr(e.mergedAttrs,b.hostAttrs),oM(t,e,n,g,b),cM(g,b,a),r!==null&&r.has(b)){let[E,R]=r.get(b);e.directiveToIndex.set(b.type,[g,E+e.directiveStart,R+e.directiveStart])}else(o===null||!o.has(b))&&e.directiveToIndex.set(b.type,g);b.contentQueries!==null&&(e.flags|=4),(b.hostBindings!==null||b.hostAttrs!==null||b.hostVars!==0)&&(e.flags|=64);let y=b.type.prototype;!m&&(y.ngOnChanges||y.ngOnInit||y.ngDoCheck)&&((t.preOrderHooks??=[]).push(e.index),m=!0),!u&&(y.ngOnChanges||y.ngDoCheck)&&((t.preOrderCheckHooks??=[]).push(e.index),u=!0),g++}aM(t,e,o)}function aM(t,n,e){for(let i=n.directiveStart;i<n.directiveEnd;i++){let a=t.data[i];if(e===null||!e.has(a))jy(0,n,a,i),jy(1,n,a,i),Hy(n,i,!1);else{let o=e.get(a);zy(0,n,o,i),zy(1,n,o,i),Hy(n,i,!0)}}}function jy(t,n,e,i){let a=t===0?e.inputs:e.outputs;for(let o in a)if(Object.hasOwn(a,o)){let r;t===0?r=n.inputs??={}:r=n.outputs??={},r[o]??=[],r[o].push(i),w0(n,o)}}function zy(t,n,e,i){let a=t===0?e.inputs:e.outputs;for(let o in a)if(Object.hasOwn(a,o)){let r=a[o],s;t===0?s=n.hostDirectiveInputs??={}:s=n.hostDirectiveOutputs??={},s[r]??=[],s[r].push(i,o),w0(n,r)}}function w0(t,n){n===`class`?t.flags|=8:n===`style`&&(t.flags|=16)}function Hy(t,n,e){let{attrs:i,inputs:a,hostDirectiveInputs:o}=t;if(i===null||!e&&a===null||e&&o===null||mf(t)){t.initialInputs??=[],t.initialInputs.push(null);return}let r=null,s=0;for(;s<i.length;){let c=i[s];if(c===0){s+=4;continue}else if(c===5){s+=2;continue}else if(typeof c==`number`)break;if(!e&&Object.hasOwn(a,c)){let m=a[c];for(let u of m)if(u===n){r??=[],r.push(c,i[s+1]);break}}else if(e&&Object.hasOwn(o,c)){let m=o[c];for(let u=0;u<m.length;u+=2)if(m[u]===n){r??=[],r.push(m[u+1],i[s+1]);break}}s+=2}t.initialInputs??=[],t.initialInputs.push(r)}function oM(t,n,e,i,a){t.data[i]=a;let r=new fo(a.factory||(a.factory=ua(a.type,!0)),Qn(a),je,null);t.blueprint[i]=r,e[i]=r,rM(t,n,i,Z_(t,e,a.hostVars,xt),a)}function rM(t,n,e,i,a){let o=a.hostBindings;if(o){let r=t.hostBindingOpCodes;r===null&&(r=t.hostBindingOpCodes=[]);let s=~n.index;sM(r)!=s&&r.push(s),r.push(e,i,o)}}function sM(t){let n=t.length;for(;n>0;){let e=t[--n];if(typeof e==`number`&&e<0)return e}return 0}function cM(t,n,e){if(e){if(n.exportAs)for(let i=0;i<n.exportAs.length;i++)e[n.exportAs[i]]=t;Qn(n)&&(e[``]=t)}}function lM(t,n,e){t.flags|=1,t.directiveStart=n,t.directiveEnd=n+e,t.providerIndexes=n}function Tf(t,n,e,i,a,o,r,s){let c=n[fe],m=c.consts,g=vo(c,t,e,i,yn(m,r));return o&&S0(c,n,g,yn(m,s),a),g.mergedAttrs=pr(g.mergedAttrs,g.attrs),g.attrs!==null&&Id(g,g.attrs,!1),g.mergedAttrs!==null&&Id(g,g.mergedAttrs,!0),c.queries!==null&&c.queries.elementStart(c,g),g}function Af(t,n){s_(t,n),Rp(n)&&t.queries.elementEnd(n)}function dM(t,n,e,i,a,o){let r=n.consts,c=vo(n,t,e,i,yn(r,a));if(c.mergedAttrs=pr(c.mergedAttrs,c.attrs),o!=null){let m=yn(r,o);c.localNames=[];for(let u=0;u<m.length;u+=2)c.localNames.push(m[u],-1)}return c.attrs!==null&&Id(c,c.attrs,!1),c.mergedAttrs!==null&&Id(c,c.mergedAttrs,!0),n.queries!==null&&n.queries.elementStart(n,c),c}var E0=typeof ShadowRoot<`u`;var uM=typeof Document<`u`;function mM(t){return Object.keys(t).map(n=>{let[e,i,a]=t[n],o={propName:e,templateName:n,isSignal:(i&Qs.SignalBased)!==0};return a&&(o.transform=a),o})}function pM(t){return Object.keys(t).map(n=>({propName:t[n],templateName:n}))}function hM(t,n,e){let i=n instanceof Et?n:n?.injector;return i&&t.getStandaloneInjector!==null&&(i=t.getStandaloneInjector(i)||i),i?new Ed(e,i):e}function fM(t){let n=t.get(kt,null);if(n===null)throw new $(407,!1);return{rendererFactory:n,sanitizer:t.get(y0,null),changeDetectionScheduler:t.get(gi,null),ngReflect:!1,tracingService:t.get(ni,null,{optional:!0})}}function gM(t,n,e){let i=D0(t);return V_(n,i,i===`svg`?Pp:i===`math`?Hv:e)}function bM(t){if((t&&`localName`in t&&typeof t.localName==`string`?t.localName:t?.tagName)?.toLowerCase()===`script`)throw new $(905,!1)}function D0(t){return(t.selectors[0][0]||`div`).toLowerCase()}var br=class{componentDef;ngModule;selector;componentType;ngContentSelectors;isBoundToModule;cachedInputs=null;cachedOutputs=null;get inputs(){return this.cachedInputs??=mM(this.componentDef.inputs),this.cachedInputs}get outputs(){return this.cachedOutputs??=pM(this.componentDef.outputs),this.cachedOutputs}constructor(n,e){this.componentDef=n,this.ngModule=e,this.componentType=n.type,this.selector=Vk(n.selectors),this.ngContentSelectors=n.ngContentSelectors??[],this.isBoundToModule=!!e}create(n,e,i,a,o,r,s,c){Ye(Be.DynamicComponentStart);let m=pe(null);try{let u=this.componentDef,g=hM(u,a||this.ngModule,n),v=fM(g),b=v.tracingService;return b&&b.componentCreate?b.componentCreate(_0(u),()=>this.createComponentRef(v,g,e,i,o,r,s,c)):this.createComponentRef(v,g,e,i,o,r,s,c)}finally{pe(m)}}createComponentRef(n,e,i,a,o,r,s,c){let m=this.componentDef,u=vM(a,m,r,o,c),g=n.rendererFactory.createRenderer(null,m),v=a?cI(g,a,m.encapsulation,e):gM(m,g,s??null);bM(v);let b=e.get(yo,null),y=yM(v,()=>e.get(ce,null)??E_());b&&b.addHost(y);let E=r?.some(Uy)||o?.some(j=>typeof j!=`function`&&j.bindings.some(Uy)),R=xf(null,u,null,512|X_(m),null,null,n,g,e,null,N_(v,e,!0));b&&E0&&y instanceof ShadowRoot&&td(R,()=>{b.removeHost(y)}),R[it]=v,rd(R);let oe=null;try{let j=Tf(it,R,2,`#host`,()=>u.directiveRegistry,!0,0);z_(g,v,j),hr(v,R),zd(u,R,j),of(u,j,R),Af(u,j),i!==void 0&&xM(j,this.ngContentSelectors,i),oe=An(j.index,R),R[gt]=oe[gt],Df(u,R,null)}catch(j){throw oe!==null&&_h(oe),_h(R),j}finally{Ye(Be.DynamicComponentEnd),sd()}return new Md(this.componentType,R,!!E)}};function vM(t,n,e,i,a){let o=t?[`ng-version`,`22.2.1`]:Bk(n.selectors[0]),r=null,s=null,c=0;if(e)for(let g of e)c+=g[Ph].requiredVars,g.create&&(g.targetIdx=0,(r??=[]).push(g)),g.update&&(g.targetIdx=0,(s??=[]).push(g));if(i)for(let g=0;g<i.length;g++){let v=i[g];if(typeof v!=`function`)for(let b of v.bindings){c+=b[Ph].requiredVars;let y=g+1;b.create&&(b.targetIdx=y,(r??=[]).push(b)),b.update&&(b.targetIdx=y,(s??=[]).push(b))}}let m=[n];if(i)for(let g of i){let b=Es(typeof g==`function`?g:g.type);m.push(b)}return _f(0,null,_M(r,s),1,c,m,null,null,null,[o],null)}function yM(t,n){let e=t.getRootNode?.();return uM&&e instanceof Document?e.head:e&&E0&&e instanceof ShadowRoot?e:n().head}function _M(t,n){return!t&&!n?null:e=>{if(e&1&&t)for(let i of t)i.create();if(e&2&&n)for(let i of n)i.update()}}function Uy(t){let n=t[Ph].kind;return n===`input`||n===`twoWay`}var Md=class extends v0{_rootLView;_hasInputBindings;instance;hostView;changeDetectorRef;componentType;location;previousInputValues=null;_tNode;constructor(n,e,i){super(),this._rootLView=e,this._hasInputBindings=i,this._tNode=Zl(e[fe],it),this.location=xr(this._tNode,e),this.instance=An(this._tNode.index,e)[gt],this.hostView=this.changeDetectorRef=new xa(e,void 0),this.componentType=n}setInput(n,e){this._hasInputBindings;let i=this._tNode;if(this.previousInputValues??=new Map,this.previousInputValues.has(n)&&Object.is(this.previousInputValues.get(n),e))return;let a=this._rootLView;Hd(i,a[fe],a,n,e);this.previousInputValues.set(n,e);kf(An(i.index,a),1)}get injector(){return new $i(this._tNode,this._rootLView)}destroy(){this.hostView.destroy()}onDestroy(n){this.hostView.onDestroy(n)}};function xM(t,n,e){let i=t.projection=[];for(let a=0;a<n.length;a++){let o=e[a];i.push(o!=null&&o.length?Array.from(o):null)}}var Lt=(()=>{class t{static __NG_ELEMENT_ID__=CM}return t})();function CM(){return k0(Dt(),de())}var Fh=class t extends Lt{_lContainer;_hostTNode;_hostLView;constructor(n,e,i){super(),this._lContainer=n,this._hostTNode=e,this._hostLView=i}get element(){return xr(this._hostTNode,this._hostLView)}get injector(){return new $i(this._hostTNode,this._hostLView)}get parentInjector(){let n=ef(this._hostTNode,this._hostLView);if(d_(n)){let e=xd(n,this._hostLView),i=_d(n),a=e[fe].data[i+8];return new $i(a,e)}else return new $i(null,this._hostLView)}clear(){for(;this.length>0;)this.remove(this.length-1)}get(n){let e=$y(this._lContainer);return e!==null&&e[n]||null}get length(){return this._lContainer.length-bt}createEmbeddedView(n,e,i){let a,o,r;typeof i==`number`?a=i:i!=null&&(a=i.index,o=i.injector,r=i.onError);let s=wd(this._lContainer,n.ssrId),c=n.createEmbeddedViewImpl(e||{},o,s);return r&&(c._lView[or]=r),this.insertImpl(c,a,fr(this._hostTNode,s)),c}createComponent(n,e,i,a,o,r,s){let c,m,u=e||{};c=u.index,i=u.injector,a=u.projectableNodes,o=u.environmentInjector||u.ngModuleRef,r=u.directives,s=u.bindings,m=u.onError;let g=new br(ma(n)),v=i||this.parentInjector;if(!o&&g.ngModule==null){let j=this.parentInjector.get(Et,null);j&&(o=j)}let b=ma(g.componentType??{}),y=wd(this._lContainer,b?.id??null),E=y?.firstChild??null,R=g.create(v,a,E,o,r,s,this._getHostElementNamespace());return m&&(R.hostView._lView[or]=m),this.insertImpl(R.hostView,c,fr(this._hostTNode,y)),R}_getHostElementNamespace(){if(this._hostTNode.type&2){let n=this._hostTNode.parent??this._hostLView[Ht];return n!==null&&n.type&2&&typeof n.value==`string`&&n.value.toLowerCase()===`foreignobject`?null:n?.namespace??null}return this._hostTNode.namespace}insert(n,e){return this.insertImpl(n,e,!0)}insertImpl(n,e,i){let a=n._lView;if($v(a)){let s=this.indexOf(n);if(s!==-1)this.detach(s);else{let c=a[_t],m=new t(c,c[Ht],c[_t]);m.detach(m.indexOf(n))}}let o=this._adjustIndex(e),r=this._lContainer;return Js(r,a,o,i),n.attachToViewContainerRef(),Dp(ch(r),o,n),n}move(n,e){return this.insert(n,e)}indexOf(n){let e=$y(this._lContainer);return e!==null?e.indexOf(n):-1}remove(n){let e=this._adjustIndex(n,-1),i=$s(this._lContainer,e);i&&(Ds(ch(this._lContainer),e),Bd(i[fe],i))}detach(n){let e=this._adjustIndex(n,-1),i=$s(this._lContainer,e);return i&&Ds(ch(this._lContainer),e)!=null?new xa(i):null}_adjustIndex(n,e=0){return n??this.length+e}};function $y(t){return t[Ms]}function ch(t){return t[Ms]||(t[Ms]=[])}function k0(t,n){let e,i=n[t.index];return Tn(i)?e=i:(e=p0(i,n,null,t),n[t.index]=e,Cf(n,e)),wM(e,n,t,i),new Fh(e,t,n)}function SM(t,n){let e=t[Je],i=e.createComment(``),a=vn(n,t);return po(e,e.parentNode(a),i,e.nextSibling(a),!1),i}var wM=kM;var EM=()=>!1;function DM(t,n,e){return EM(t,n,e)}function kM(t,n,e,i){if(t[oo])return;let a;e.type&8?a=Ut(i):a=SM(n,e),t[oo]=a}var Lh=class t{queryList;matches=null;constructor(n){this.queryList=n}clone(){return new t(this.queryList)}setDirty(){this.queryList.setDirty()}};var Vh=class t{queries;constructor(n=[]){this.queries=n}createEmbeddedView(n){let e=n.queries;if(e!==null){let i=n.contentQueries!==null?n.contentQueries[0]:e.length,a=[];for(let o=0;o<i;o++){let r=e.getByIndex(o),s=this.queries[r.indexInDeclarationView];a.push(s.clone())}return new t(a)}return null}insertView(n){this.dirtyQueriesWithMatches(n)}detachView(n){this.dirtyQueriesWithMatches(n)}finishViewCreation(n){this.dirtyQueriesWithMatches(n)}dirtyQueriesWithMatches(n){for(let e=0;e<this.queries.length;e++)Rf(n,e).matches!==null&&this.queries[e].setDirty()}};var Nd=class{flags;read;predicate;constructor(n,e,i=null){this.flags=e,this.read=i,typeof n==`string`?this.predicate=AM(n):this.predicate=n}};var Bh=class t{queries;constructor(n=[]){this.queries=n}elementStart(n,e){for(let i=0;i<this.queries.length;i++)this.queries[i].elementStart(n,e)}elementEnd(n){for(let e=0;e<this.queries.length;e++)this.queries[e].elementEnd(n)}embeddedTView(n){let e=null;for(let i=0;i<this.length;i++){let a=e!==null?e.length:0,o=this.getByIndex(i).embeddedTView(n,a);o&&(o.indexInDeclarationView=i,e!==null?e.push(o):e=[o])}return e!==null?new t(e):null}template(n,e){for(let i=0;i<this.queries.length;i++)this.queries[i].template(n,e)}getByIndex(n){return this.queries[n]}get length(){return this.queries.length}track(n){this.queries.push(n)}};var jh=class t{metadata;matches=null;indexInDeclarationView=-1;crossesNgTemplate=!1;_declarationNodeIndex;_appliesToNextNode=!0;constructor(n,e=-1){this.metadata=n,this._declarationNodeIndex=e}elementStart(n,e){this.isApplyingToNode(e)&&this.matchTNode(n,e)}elementEnd(n){this._declarationNodeIndex===n.index&&(this._appliesToNextNode=!1)}template(n,e){this.elementStart(n,e)}embeddedTView(n,e){return this.isApplyingToNode(n)?(this.crossesNgTemplate=!0,this.addMatch(-n.index,e),new t(this.metadata)):null}isApplyingToNode(n){if(this._appliesToNextNode&&(this.metadata.flags&1)!==1){let e=this._declarationNodeIndex,i=n.parent;for(;i!==null&&i.type&8&&i.index!==e;)i=i.parent;return e===(i!==null?i.index:-1)}return this._appliesToNextNode}matchTNode(n,e){let i=this.metadata.predicate;if(Array.isArray(i))for(let a=0;a<i.length;a++){let o=i[a];this.matchTNodeWithReadOption(n,e,IM(e,o)),this.matchTNodeWithReadOption(n,e,bd(e,n,o,!1,!1))}else i===Ft?e.type&4&&this.matchTNodeWithReadOption(n,e,-1):this.matchTNodeWithReadOption(n,e,bd(e,n,i,!1,!1))}matchTNodeWithReadOption(n,e,i){if(i!==null){let a=this.metadata.read;if(a!==null)if(a===G||a===Lt||a===re||a===Ft&&e.type&4)this.addMatch(e.index,-2);else{let o=bd(e,n,a,!1,!1);o!==null&&this.addMatch(e.index,o)}else this.addMatch(e.index,i)}}addMatch(n,e){this.matches===null?this.matches=[n,e]:this.matches.push(n,e)}};function IM(t,n){let e=t.localNames;if(e!==null){for(let i=0;i<e.length;i+=2)if(e[i]===n)return e[i+1]}return null}function MM(t,n){return t.type&11?xr(t,n):t.type&4?Ud(t,n):null}function NM(t,n,e,i){return e===-1?MM(n,t):e===-2?TM(t,n,i):zs(t,t[fe],e,n)}function TM(t,n,e){if(e===G)return xr(n,t);if(e===Ft)return Ud(n,t);if(e===Lt)return k0(n,t);if(e===re)return new $i(n,t)}function I0(t,n,e,i){let a=n[vi].queries[i];if(a.matches===null){let o=t.data,r=e.matches,s=[];for(let c=0;r!==null&&c<r.length;c+=2){let m=r[c];if(m<0)s.push(null);else{let u=o[m];s.push(NM(n,u,r[c+1],e.metadata.read))}}a.matches=s}return a.matches}function zh(t,n,e,i){let a=t.queries.getByIndex(e),o=a.matches;if(o!==null){let r=I0(t,n,a,e);for(let s=0;s<o.length;s+=2){let c=o[s];if(c>0)i.push(r[s/2]);else{let m=o[s+1],u=n[-c];for(let g=bt;g<u.length;g++){let v=u[g];v[fa]===v[_t]&&zh(v[fe],v,m,i)}if(u[ro]!==null){let g=u[ro];for(let v=0;v<g.length;v++){let b=g[v];zh(b[fe],b,m,i)}}}}}return i}function Of(t,n){return t[vi].queries[n].queryList}function M0(t,n,e){let i=new _n((e&4)===4);return qv(t,n,i,i.destroy),(n[vi]??=new Vh).queries.push(new Lh(i))-1}function N0(t,n,e){let i=Qe();return i.firstCreatePass&&(A0(i,new Nd(t,n,e),-1),(n&2)===2&&(i.staticViewQueries=!0)),M0(i,de(),n)}function T0(t,n,e,i){let a=Qe();if(a.firstCreatePass){let o=Dt();A0(a,new Nd(n,e,i),o.index),OM(a,t),(e&2)===2&&(a.staticContentQueries=!0)}return M0(a,de(),e)}function AM(t){return t.split(`,`).map(n=>n.trim())}function A0(t,n,e){t.queries===null&&(t.queries=new Bh),t.queries.track(new jh(n,e))}function OM(t,n){let e=t.contentQueries||(t.contentQueries=[]);n!==(e.length?e[e.length-1]:-1)&&e.push(t.queries.length-1,n)}function Rf(t,n){return t.queries.getByIndex(n)}function O0(t,n){let e=t[fe],i=Rf(e,n);return i.crossesNgTemplate?zh(e,t,n,[]):I0(e,t,i,n)}function R0(t,n,e){let i,a=us(()=>{i._dirtyCounter();let o=RM(i,t);if(n&&o===void 0)throw new $(-951,!1);return o});return i=a[yt],i._dirtyCounter=M(0),i._flatValue=void 0,a}function Pf(t){return R0(!0,!1,t)}function Ff(t){return R0(!0,!0,t)}function P0(t,n){let e=t[yt];e._lView=de(),e._queryIndex=n,e._queryList=Of(e._lView,n),e._queryList.onDirty(()=>e._dirtyCounter.update(i=>i+1))}function RM(t,n){let e=t._lView,i=t._queryIndex;if(e===void 0||i===void 0||e[_e]&4)return n?void 0:Yt;let a=Of(e,i),o=O0(e,i);return a.reset(o,y_),n?a.first:a._changesDetected||t._flatValue===void 0?t._flatValue=a.toArray():t._flatValue}function _o(t){return!!t&&typeof t.then==`function`}function Lf(t){return!!t&&typeof t.subscribe==`function`}var ti=class{};function Vf(t,n){return new Td(t,n??null,[])}var Td=class extends ti{ngModuleType;_parent;_bootstrapComponents=[];_r3Injector;instance;destroyCbs=[];constructor(n,e,i,a=!0){super(),this.ngModuleType=n,this._parent=e;let o=Ev(n);this._bootstrapComponents=kk(o.bootstrap),this._r3Injector=Jp(n,e,[{provide:ti,useValue:this},...i],Cs(n),new Set([`environment`])),a&&this.resolveInjectorInitializers()}resolveInjectorInitializers(){this._r3Injector.resolveInjectorInitializers(),this.instance=this._r3Injector.get(this.ngModuleType)}get injector(){return this._r3Injector}destroy(){let n=this._r3Injector;!n.destroyed&&n.destroy(),this.destroyCbs.forEach(e=>e()),this.destroyCbs=null}onDestroy(n){this.destroyCbs.push(n)}};var Gs=class extends ti{injector;instance=null;constructor(n){super();let e=new Ja([...n.providers,{provide:ti,useValue:this}],n.parent||tr(),n.debugName,new Set([`environment`]));this.injector=e,n.runEnvironmentInitializers&&e.resolveInjectorInitializers()}destroy(){this.injector.destroy()}onDestroy(n){this.injector.onDestroy(n)}};function F0(t,n,e=null){return new Gs({providers:t,parent:n,debugName:e,runEnvironmentInitializers:!0}).injector}var PM=(()=>{class t{_injector;cachedInjectors=new Map;constructor(e){this._injector=e}getOrCreateStandaloneInjector(e){if(!e.standalone)return null;if(!this.cachedInjectors.has(e)){let i=Ip(!1,e.type),a=i.length>0?F0([i],this._injector,``):null;this.cachedInjectors.set(e,a)}return this.cachedInjectors.get(e)}ngOnDestroy(){try{for(let e of this.cachedInjectors.values())e!==null&&e.destroy()}finally{this.cachedInjectors.clear()}}static ɵprov=he({token:t,providedIn:`environment`,factory:()=>new t(Se(Et))})}return t})();function B(t){return qs(()=>{let n=L0(t),e=s(r({},n),{decls:t.decls,vars:t.vars,template:t.template,consts:t.consts||null,ngContentSelectors:t.ngContentSelectors,onPush:t.changeDetection!==nf.Eager,directiveDefs:null,pipeDefs:null,dependencies:n.standalone&&t.dependencies||null,getStandaloneInjector:n.standalone?a=>a.get(PM).getOrCreateStandaloneInjector(e):null,getExternalStyles:null,signals:t.signals??!1,data:t.data||{},encapsulation:t.encapsulation||Jn.Emulated,styles:t.styles||Yt,_:null,schemas:t.schemas||null,tView:null,id:``});n.standalone&&Ci(`NgStandalone`),V0(e);let i=t.dependencies;return e.directiveDefs=Gy(i,FM),e.pipeDefs=Gy(i,Dv),e.id=BM(e),e})}function FM(t){return ma(t)||Es(t)}function ie(t){return qs(()=>({type:t.type,bootstrap:t.bootstrap||Yt,declarations:t.declarations||Yt,imports:t.imports||Yt,exports:t.exports||Yt,transitiveCompileScopes:null,schemas:t.schemas||null,id:t.id||null}))}function LM(t,n){if(t==null)return pa;let e={};for(let i in t)if(Object.hasOwn(t,i)){let a=t[i],o,r,s,c;Array.isArray(a)?(s=a[0],o=a[1],r=a[2]??o,c=a[3]||null):(o=a,r=a,s=Qs.None,c=null),e[o]=[i,s,c],n[o]=r}return e}function VM(t){if(t==null)return pa;let n={};for(let e in t)Object.hasOwn(t,e)&&(n[t[e]]=e);return n}function q(t){return qs(()=>{let n=L0(t);return V0(n),n})}function ec(t){return{type:t.type,name:t.name,factory:null,pure:t.pure!==!1,standalone:t.standalone??!0,onDestroy:t.type.prototype.ngOnDestroy||null}}function L0(t){let n={};return{type:t.type,providersResolver:null,viewProvidersResolver:null,factory:null,hostBindings:t.hostBindings||null,hostVars:t.hostVars||0,hostAttrs:t.hostAttrs||null,contentQueries:t.contentQueries||null,declaredInputs:n,inputConfig:t.inputs||pa,exportAs:t.exportAs||null,standalone:t.standalone??!0,signals:t.signals===!0,selectors:t.selectors||Yt,viewQuery:t.viewQuery||null,features:t.features||null,setInput:null,resolveHostDirectives:null,hostDirectives:null,controlDef:null,signalFormsInputPresence:null,inputs:LM(t.inputs,n),outputs:VM(t.outputs),debugInfo:null}}function V0(t){t.features?.forEach(n=>n(t))}function Gy(t,n){return t?()=>{let e=typeof t==`function`?t():t,i=[];for(let a of e){let o=n(a);o!==null&&i.push(o)}return i}:null}function BM(t){let n=0,e=typeof t.consts==`function`?``:t.consts,i=[t.selectors,t.ngContentSelectors,t.hostVars,t.hostAttrs,e,t.vars,t.decls,t.encapsulation,t.standalone,t.signals,t.exportAs,JSON.stringify(t.inputs),JSON.stringify(t.outputs),Object.getOwnPropertyNames(t.type.prototype),!!t.contentQueries,!!t.viewQuery];for(let o of i.join(`|`))n=Math.imul(31,n)+o.charCodeAt(0)<<0;return n+=2147483648,`c`+n}var B0=new D(``);var Bf=(()=>{class t{resolve;reject;initialized=!1;done=!1;donePromise=new Promise((e,i)=>{this.resolve=e,this.reject=i});appInits=f(B0,{optional:!0})??[];injector=f(re);constructor(){}runInitializers(){if(this.initialized)return;let e=[];for(let a of this.appInits){let o=nr(this.injector,a);if(_o(o))e.push(o);else if(Lf(o)){let r=new Promise((s,c)=>{o.subscribe({complete:s,error:c})});e.push(r)}}let i=()=>{this.done=!0,this.resolve()};Promise.all(e).then(()=>{i()}).catch(a=>{this.reject(a)}),e.length===0&&i(),this.initialized=!0}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();function jf(t){return n=>{n.controlDef={create:(e,i)=>{e?.ɵngControlCreate(i)},update:(e,i)=>{e?.ɵngControlUpdate?.(i)},passThroughInput:t}}}function tc(t){let n=e=>{let i=Array.isArray(t);e.hostDirectives===null?(e.resolveHostDirectives=jM,e.hostDirectives=i?t.map(Hh):[t]):i?e.hostDirectives.unshift(...t.map(Hh)):e.hostDirectives.unshift(t)};return n.ngInherit=!0,n}function jM(t){let n=[],e=!1,i=null,a=null;for(let o=0;o<t.length;o++){let r=t[o];if(r.hostDirectives!==null){let s=n.length;i??=new Map,a??=new Map,j0(r,n,i,t),a.set(r,[s,n.length-1])}o===0&&Qn(r)&&(e=!0,n.push(r))}for(let o=e?1:0;o<t.length;o++)n.push(t[o]);return i!==null&&i.forEach((o,r)=>{zM(r.declaredInputs,o.inputs)}),[n,i,a]}function j0(t,n,e,i){if(t.hostDirectives!==null)for(let a of t.hostDirectives)if(typeof a==`function`){let o=a();for(let r of o)Wy(Hh(r),n,e,i)}else Wy(a,n,e,i)}function Wy(t,n,e,i){let a=Es(t.directive);if(j0(a,n,e,i),e.has(a)){let o=e.get(a);qy(o,t.inputs,`input`),qy(o,t.outputs,`output`)}else i.includes(a)||(e.set(a,t),n.push(a))}function qy(t,n,e){let i=e===`input`?t.inputs:t.outputs;Object.keys(n).forEach(a=>{let o=n[a];(!Object.hasOwn(i,a)||i[a]===o)&&(i[a]=o)})}function Hh(t){return typeof t==`function`?{directive:Ot(t),inputs:{},outputs:{}}:{directive:Ot(t.directive),inputs:Yy(t.inputs),outputs:Yy(t.outputs)}}function Yy(t){let n={};if(t!==void 0&&t.length>0)for(let e=0;e<t.length;e+=2)n[t[e]]=t[e+1];return n}function zM(t,n){for(let e in n)if(Object.hasOwn(n,e)){let i=n[e];t[i]=t[e]}}function HM(t){return Object.getPrototypeOf(t.prototype).constructor}function Fe(t){let n=HM(t.type),e=!0,i=[t];for(;n&&n!==Function.prototype&&n!==Object.prototype;){let a,o=Object.hasOwn(n,Ss)?n[Ss]:void 0,r=Object.hasOwn(n,ws)?n[ws]:void 0;if(Qn(t))a=o??r;else{if(o)throw new $(903,!1);a=r}if(a){if(e){i.push(a);let c=t;c.inputs=lh(t.inputs),c.declaredInputs=lh(t.declaredInputs),c.outputs=lh(t.outputs);let m=a.hostBindings;m&&qM(t,m);let u=a.viewQuery,g=a.contentQueries;if(u&&GM(t,u),g&&WM(t,g),UM(t,a),wv(t.outputs,a.outputs),Qn(a)&&a.data.animation){let v=t.data;v.animation=(v.animation||[]).concat(a.data.animation)}}let s=a.features;if(s)for(let c=0;c<s.length;c++){let m=s[c];m&&m.ngInherit&&m(t),m===Fe&&(e=!1)}}n=Object.getPrototypeOf(n)}$M(i)}function UM(t,n){for(let e in n.inputs){if(!Object.hasOwn(n.inputs,e)||Object.hasOwn(t.inputs,e))continue;let i=n.inputs[e];i!==void 0&&(t.inputs[e]=i,t.declaredInputs[e]=n.declaredInputs[e])}}function $M(t){let n=0,e=null;for(let i=t.length-1;i>=0;i--){let a=t[i];a.hostVars=n+=a.hostVars,a.hostAttrs=pr(a.hostAttrs,e=pr(e,a.hostAttrs))}}function lh(t){return t===pa?{}:t===Yt?[]:t}function GM(t,n){let e=t.viewQuery;e?t.viewQuery=(i,a)=>{n(i,a),e(i,a)}:t.viewQuery=n}function WM(t,n){let e=t.contentQueries;e?t.contentQueries=(i,a,o)=>{n(i,a,o),e(i,a,o)}:t.contentQueries=n}function qM(t,n){let e=t.hostBindings;e?t.hostBindings=(i,a)=>{n(i,a),e(i,a)}:t.hostBindings=n}function z0(t,n,e,i,a,o,r,s){if(e.firstCreatePass){t.mergedAttrs=pr(t.mergedAttrs,t.attrs);let u=t.tView=_f(2,t,a,o,r,e.directiveRegistry,e.pipeRegistry,null,e.schemas,e.consts,null);e.queries!==null&&(e.queries.template(e,t),u.queries=e.queries.embeddedTView(t))}s&&(t.flags|=s),lo(t,!1);let c=KM(e,n,t,i);cd()&&vf(e,n,c,t),hr(c,n);let m=p0(c,n,c,t);n[i+it]=m,Cf(n,m),DM(m,t,n)}function YM(t,n,e,i,a,o,r,s,c,m,u){let g=e+it,v;return n.firstCreatePass?(v=vo(n,g,4,r||null,s||null),nd()&&S0(n,t,v,yn(n.consts,m),Sf),s_(n,v)):v=n.data[g],z0(v,t,n,e,i,a,o,c),rr(v)&&zd(n,t,v),m!=null&&Xs(t,v,u),v}function vr(t,n,e,i,a,o,r,s,c,m,u){let g=e+it,v;if(n.firstCreatePass){if(v=vo(n,g,4,r||null,s||null),m!=null){let b=yn(n.consts,m);v.localNames=[];for(let y=0;y<b.length;y+=2)v.localNames.push(b[y],-1)}}else v=n.data[g];return z0(v,t,n,e,i,a,o,c),m!=null&&Xs(t,v,u),v}function at(t,n,e,i,a,o,r,s){let c=de(),m=Qe();return YM(c,m,t,n,e,i,a,yn(m.consts,o),void 0,r,s),at}function wr(t,n,e,i,a,o,r,s){let c=de(),m=Qe();return vr(c,m,t,n,e,i,a,yn(m.consts,o),void 0,r,s),wr}var KM=QM;function QM(t,n,e,i){return Ps(!0),n[Je].createComment(``)}var zf=new D(``);var Hf=new D(``);function H0(){zm(t=>{throw new $(-600,``)})}var XM=10;var On=(()=>{class t{_runningTick=!1;_destroyed=!1;_destroyListeners=[];_views=[];internalErrorHandler=f(zi);afterRenderManager=f(Vd);zonelessEnabled=f(Ls);rootEffectScheduler=f(ud);dirtyFlags=0;tracingSnapshot=null;allTestViews=new Set;autoDetectTestViews=new Set;includeAllTestViews=!1;afterTick=new L;get allViews(){return[...(this.includeAllTestViews?this.allTestViews:this.autoDetectTestViews).keys(),...this._views]}get destroyed(){return this._destroyed}componentTypes=[];components=[];internalPendingTask=f(mo);get isStable(){return this.internalPendingTask.hasPendingTasksObservable.pipe(ot(e=>!e))}constructor(){f(ni,{optional:!0})}whenStable(){let e;return new Promise(i=>{e=this.isStable.subscribe({next:a=>{a&&i()}})}).finally(()=>{e.unsubscribe()})}_injector=f(Et);_rendererFactory=null;get injector(){return this._injector}bootstrap(e,i){return this.bootstrapImpl(e,i)}bootstrapImpl(e,i,a=re.NULL){return this._injector.get(K).run(()=>{if(Ye(Be.BootstrapComponentStart),!this._injector.get(Bf).done)throw new $(405,``);let s=ma(e),c=this._injector.get(ti),m=new br(s,c);this.componentTypes.push(e);let{hostElement:u,directives:g,bindings:v}=ZM(i),b=u||m.selector,y=m.create(a,[],b,c.injector,g,v),E=y.location.nativeElement,R=y.injector.get(zf,null);return R?.registerApplication(E),y.onDestroy(()=>{this.detachView(y.hostView),js(this.components,y),R?.unregisterApplication(E)}),this._loadComponent(y),Ye(Be.BootstrapComponentEnd,y),y})}tick(){this.zonelessEnabled||(this.dirtyFlags|=1),this._tick()}_tick(){Ye(Be.ChangeDetectionStart),this.tracingSnapshot!==null?this.tracingSnapshot.run(Ld.CHANGE_DETECTION,this.tickImpl):this.tickImpl()}tickImpl=()=>{if(this._runningTick)throw Ye(Be.ChangeDetectionEnd),new $(101,!1);let e=pe(null);try{this._runningTick=!0,this.synchronize()}finally{this._runningTick=!1,this.tracingSnapshot?.dispose(),this.tracingSnapshot=null,pe(e),this.afterTick.next(),Ye(Be.ChangeDetectionEnd)}};synchronize(){this._rendererFactory===null&&!this._injector.destroyed&&(this._rendererFactory=this._injector.get(kt,null,{optional:!0}));let e=0;for(;this.dirtyFlags!==0&&e++<XM;){Ye(Be.ChangeDetectionSyncStart);try{this.synchronizeOnce()}finally{Ye(Be.ChangeDetectionSyncEnd)}}}synchronizeOnce(){this.dirtyFlags&16&&(this.dirtyFlags&=-17,this.rootEffectScheduler.flush());let e=!1;if(this.dirtyFlags&7){let i=!!(this.dirtyFlags&1);this.dirtyFlags&=-8,this.dirtyFlags|=8;for(let{_lView:a}of this.allViews){if(!i&&!As(a))continue;l0(a,i&&!this.zonelessEnabled?0:1),e=!0}if(this.dirtyFlags&=-5,this.syncDirtyFlagsWithViews(),this.dirtyFlags&23)return}e||(this._rendererFactory?.begin?.(),this._rendererFactory?.end?.()),this.dirtyFlags&8&&(this.dirtyFlags&=-9,this.afterRenderManager.execute()),this.syncDirtyFlagsWithViews()}syncDirtyFlagsWithViews(){if(this.allViews.some(({_lView:e})=>As(e))){this.dirtyFlags|=2;return}else this.dirtyFlags&=-8}attachView(e){let i=e;this._views.push(i),i.attachToAppRef(this)}detachView(e){let i=e;js(this._views,i),i.detachFromAppRef()}_loadComponent(e){this.attachView(e.hostView);try{this.tick()}catch(a){this.internalErrorHandler(a)}this.components.push(e),this._injector.get(Hf,[]).forEach(a=>a(e))}ngOnDestroy(){if(!this._destroyed)try{this._destroyListeners.forEach(e=>e()),this._views.slice().forEach(e=>e.destroy())}finally{this._destroyed=!0,this._views=[],this._destroyListeners=[]}}onDestroy(e){return this._destroyListeners.push(e),()=>js(this._destroyListeners,e)}destroy(){if(this._destroyed)throw new $(406,!1);let e=this._injector;e.destroy&&!e.destroyed&&e.destroy()}get viewCount(){return this._views.length}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();function ZM(t){return t===void 0||typeof t==`string`||t instanceof Element?{hostElement:t}:t}function js(t,n){let e=t.indexOf(n);e>-1&&t.splice(e,1)}function Er(t,n){let e=de();if(xn(e,ga(),n)){let a=Qe(),o=cr();if(Hd(o,a,e,t,n))yi(o)&&n0(e,o.index);else{let s=vn(o,e);i0(e[Je],s,null,o.value,t,n,null)}}return Er}function ee(t,n,e,i){let a=de();if(xn(a,ga(),n)){Qe();gI(cr(),a,t,n,e,i)}return ee}var Uh=class{destroy(n){}updateValue(n,e){}swap(n,e){let i=Math.min(n,e),a=Math.max(n,e),o=this.detach(a);if(a-i>1){let r=this.detach(i);this.attach(i,o),this.attach(a,r)}else this.attach(i,o)}move(n,e){this.attach(e,this.detach(n))}};function dh(t,n,e,i,a){return t===e&&Object.is(n,i)?1:Object.is(a(t,n),a(e,i))?-1:0}function JM(t,n,e,i){let a,o,r=0,s=t.length-1;if(Array.isArray(n)){pe(i);let m=n.length-1;for(pe(null);r<=s&&r<=m;){let u=t.at(r),g=n[r],v=dh(r,u,r,g,e);if(v!==0){v<0&&t.updateValue(r,g),r++;continue}let b=t.at(s),y=n[m],E=dh(s,b,m,y,e);if(E!==0){E<0&&t.updateValue(s,y),s--,m--;continue}let R=e(r,u),oe=e(s,b),j=e(r,g);if(Object.is(j,oe)){let x=e(m,y);Object.is(x,R)?(t.swap(r,s),t.updateValue(s,y),m--,s--):t.move(s,r),t.updateValue(r,g),r++;continue}if(a??=new Ad,o??=Qy(t,r,s,e),$h(t,a,r,j))t.updateValue(r,g),r++,s++;else if(o.has(j))a.set(R,t.detach(r)),s--;else{let x=t.create(r,n[r]);t.attach(r,x),r++,s++}}for(;r<=m;)Ky(t,a,e,r,n[r]),r++}else if(n!=null){pe(i);let m=n[Symbol.iterator]();pe(null);let u=m.next();for(;!u.done&&r<=s;){let g=t.at(r),v=u.value,b=dh(r,g,r,v,e);if(b!==0)b<0&&t.updateValue(r,v),r++,u=m.next();else{a??=new Ad,o??=Qy(t,r,s,e);let y=e(r,v);if($h(t,a,r,y))t.updateValue(r,v),r++,s++,u=m.next();else if(!o.has(y))t.attach(r,t.create(r,v)),r++,s++,u=m.next();else{let E=e(r,g);a.set(E,t.detach(r)),s--}}}for(;!u.done;)Ky(t,a,e,t.length,u.value),u=m.next()}for(;r<=s;)t.destroy(t.detach(s--));a?.forEach(m=>{t.destroy(m)})}function $h(t,n,e,i){return n!==void 0&&n.has(i)?(t.attach(e,n.get(i)),n.delete(i),!0):!1}function Ky(t,n,e,i,a){if($h(t,n,i,e(i,a)))t.updateValue(i,a);else{let o=t.create(i,a);t.attach(i,o)}}function Qy(t,n,e,i){let a=new Set;for(let o=n;o<=e;o++)a.add(i(o,t.at(o)));return a}var Ad=class{kvMap=new Map;_vMap=void 0;has(n){return this.kvMap.has(n)}delete(n){if(!this.has(n))return!1;let e=this.kvMap.get(n);return this._vMap!==void 0&&this._vMap.has(e)?(this.kvMap.set(n,this._vMap.get(e)),this._vMap.delete(e)):this.kvMap.delete(n),!0}get(n){return this.kvMap.get(n)}set(n,e){if(this.kvMap.has(n)){let i=this.kvMap.get(n);this._vMap===void 0&&(this._vMap=new Map);let a=this._vMap;for(;a.has(i);)i=a.get(i);a.set(i,e)}else this.kvMap.set(n,e)}forEach(n){for(let[e,i]of this.kvMap)if(n(i,e),this._vMap!==void 0){let a=this._vMap;for(;a.has(i);)i=a.get(i),n(i,e)}}};function N(t,n,e,i,a,o,r,s){Ci(`NgControlFlow`);let c=de(),m=Qe();return vr(c,m,t,n,e,i,a,yn(m.consts,o),256,r,s),Uf}function Uf(t,n,e,i,a,o,r,s){return Ci(`NgControlFlow`),eN(t,n,e,i,a,o,r,s),Uf}function eN(t,n,e,i,a,o,r,s){let c=de(),m=Qe();vr(c,m,t,n,e,i,a,yn(m.consts,o),512,r,s)}function T(t,n){Ci(`NgControlFlow`);let e=de(),i=ga(),a=e[i]!==xt?e[i]:-1,o=a!==-1?Od(e,it+a):void 0,r=0;if(xn(e,i,t)){let s=pe(null);try{if(o!==void 0&&f0(o,r),t!==-1){let c=it+t,m=Od(e,c),u=Yh(e[fe],c),g=b0(m,u,e);Js(m,Zs(e,u,n,{dehydratedView:g}),r,fr(u,g))}}finally{pe(s)}}else if(o!==void 0){let s=h0(o,r);s!==void 0&&(s[gt]=n)}}var Gh=class{lContainer;$implicit;$index;constructor(n,e,i){this.lContainer=n,this.$implicit=e,this.$index=i}get $count(){return this.lContainer.length-bt}};function $f(t){return t}function ii(t,n){return n}var Wh=class{hasEmptyBlock;trackByFn;liveCollection;constructor(n,e,i){this.hasEmptyBlock=n,this.trackByFn=e,this.liveCollection=i}};function ve(t,n,e,i,a,o,r,s,c,m,u,g,v){Ci(`NgControlFlow`);let b=de(),y=Qe(),E=c!==void 0,R=de(),j=new Wh(E,s?r.bind(R[Kt][gt]):r);R[it+t]=j,vr(b,y,t+1,n,e,i,a,yn(y.consts,o),256),E&&vr(b,y,t+2,c,m,u,g,yn(y.consts,v),512)}var qh=class extends Uh{lContainer;hostLView;templateTNode;operationsCounter=void 0;needsIndexUpdate=!1;constructor(n,e,i){super(),this.lContainer=n,this.hostLView=e,this.templateTNode=i}get length(){return this.lContainer.length-bt}at(n){return this.getLView(n)[gt].$implicit}attach(n,e){let i=e[no];this.needsIndexUpdate||=n!==this.length,Js(this.lContainer,e,n,fr(this.templateTNode,i)),tN(this.lContainer,n)}detach(n){return this.needsIndexUpdate||=n!==this.length-1,nN(this.lContainer,n),iN(this.lContainer,n)}create(n,e){let i=wd(this.lContainer,this.templateTNode.tView.ssrId);return Zs(this.hostLView,this.templateTNode,new Gh(this.lContainer,e,n),{dehydratedView:i})}destroy(n){Bd(n[fe],n)}updateValue(n,e){this.getLView(n)[gt].$implicit=e}reset(){this.needsIndexUpdate=!1}updateIndexes(){if(this.needsIndexUpdate)for(let n=0;n<this.length;n++)this.getLView(n)[gt].$index=n}getLView(n){return aN(this.lContainer,n)}};function ye(t){let n=pe(null),e=dn();try{let i=de(),a=i[fe],o=i[e],r=e+1,s=Od(i,r);if(o.liveCollection===void 0)o.liveCollection=new qh(s,i,Yh(a,r));else o.liveCollection.reset();let c=o.liveCollection;if(JM(c,t,o.trackByFn,n),c.updateIndexes(),o.hasEmptyBlock){let m=ga(),u=c.length===0;if(xn(i,m,u)){let g=e+2,v=Od(i,g);if(u){let b=Yh(a,g),y=b0(v,b,i);Js(v,Zs(i,b,void 0,{dehydratedView:y}),0,fr(b,y))}else a.firstUpdatePass&&jI(v),f0(v,0)}}}finally{pe(n)}}function Od(t,n){return t[n]}function tN(t,n){if(t.length<=bt)return;let i=t[bt+n];i&&Wk(i)}function nN(t,n){if(t.length<=bt)return;let i=t[bt+n];i&&Gk(i)}function iN(t,n){return $s(t,n)}function aN(t,n){return h0(t,n)}function Yh(t,n){return Zl(t,n)}function S(t,n,e){let i=de();if(xn(i,ga(),n)){Qe();mI(cr(),i,t,n,i[Je],e)}return S}function Kh(t,n,e,i,a){Hd(n,t,e,a?`class`:`style`,i)}function l(t,n,e,i){let a=de(),o=a[fe],r=t+it,s=o.firstCreatePass?Tf(r,a,2,n,Sf,nd(),e,i):o.data[r];if(yi(s)){let c=a[Kn].tracingService;if(c&&c.componentCreate){let m=o.data[s.directiveStart+s.componentOffset];return c.componentCreate(_0(m),()=>(Xy(t,n,a,s,i),l))}}return Xy(t,n,a,s,i),l}function Xy(t,n,e,i,a){if(wf(i,e,t,n,U0),rr(i)){let o=e[fe];zd(o,e,i),of(o,i,e)}a!=null&&Xs(e,i)}function d(){let t=Qe(),e=Ef(Dt());return t.firstCreatePass&&Af(t,e),Up(e)&&$p(),zp(),e.classesWithoutHost!=null&&AD(e)&&Kh(t,e,de(),e.classesWithoutHost,!0),e.stylesWithoutHost!=null&&OD(e)&&Kh(t,e,de(),e.stylesWithoutHost,!1),d}function z(t,n,e,i){return l(t,n,e,i),d(),z}function rt(t,n,e,i){let a=de(),o=a[fe],r=t+it,s=o.firstCreatePass?dM(r,o,2,n,e,i):o.data[r];return wf(s,a,t,n,U0),i!=null&&Xs(a,s),rt}function ut(){return Up(Ef(Dt()))&&$p(),zp(),ut}function Gt(t,n,e,i){return rt(t,n,e,i),ut(),Gt}var U0=(t,n,e,i,a)=>(Ps(!0),V_(n[Je],i,Zp()));function Gf(t,n,e){let i=de(),a=i[fe],o=t+it,r=a.firstCreatePass?Tf(o,i,8,`ng-container`,Sf,nd(),n,e):a.data[o];if(wf(r,i,t,`ng-container`,oN),rr(r)){let s=i[fe];zd(s,i,r),of(s,r,i)}return e!=null&&Xs(i,r),Gf}function Wf(){let t=Qe(),e=Ef(Dt());return t.firstCreatePass&&Af(t,e),Wf}function Sa(t,n,e){return Gf(t,n,e),Wf(),Sa}var oN=(t,n,e,i,a)=>(Ps(!0),Sk(n[Je],``));function ae(){return de()}function Wt(t,n,e){let i=de();if(xn(i,ga(),n)){Qe();t0(cr(),i,t,n,i[Je],e)}return Wt}var Vs=void 0;function rN(t){let n=Math.floor(Math.abs(t)),e=t.toString().replace(/^[^.]*\.?/,``).length;return n===1&&e===0?1:5}var sN=[`en`,[[`a`,`p`],[`AM`,`PM`]],[[`AM`,`PM`]],[[`S`,`M`,`T`,`W`,`T`,`F`,`S`],[`Sun`,`Mon`,`Tue`,`Wed`,`Thu`,`Fri`,`Sat`],[`Sunday`,`Monday`,`Tuesday`,`Wednesday`,`Thursday`,`Friday`,`Saturday`],[`Su`,`Mo`,`Tu`,`We`,`Th`,`Fr`,`Sa`]],Vs,[[`J`,`F`,`M`,`A`,`M`,`J`,`J`,`A`,`S`,`O`,`N`,`D`],[`Jan`,`Feb`,`Mar`,`Apr`,`May`,`Jun`,`Jul`,`Aug`,`Sep`,`Oct`,`Nov`,`Dec`],[`January`,`February`,`March`,`April`,`May`,`June`,`July`,`August`,`September`,`October`,`November`,`December`]],Vs,[[`B`,`A`],[`BC`,`AD`],[`Before Christ`,`Anno Domini`]],0,[6,0],[`M/d/yy`,`MMM d, y`,`MMMM d, y`,`EEEE, MMMM d, y`],[`h:mm a`,`h:mm:ss a`,`h:mm:ss a z`,`h:mm:ss a zzzz`],[`{1}, {0}`,Vs,Vs,Vs],[`.`,`,`,`;`,`%`,`+`,`-`,`E`,`×`,`‰`,`∞`,`NaN`,`:`],[`#,##0.###`,`#,##0%`,`¤#,##0.00`,`#E0`],`USD`,`$`,`US Dollar`,{},`ltr`,rN];var uh=Object.create(null);function un(t){let n=cN(t),e=Zy(n);if(e)return e;let i=n.split(`-`)[0];if(e=Zy(i),e)return e;if(i===`en`)return sN;throw new $(701,!1)}function Zy(t){if(!(t in uh)){let n=bi.ng&&bi.ng.common&&bi.ng.common.locales&&bi.ng.common.locales[t];return n!==void 0&&(uh[t]=n),n}return uh[t]}var Ct={LocaleId:0,DayPeriodsFormat:1,DayPeriodsStandalone:2,DaysFormat:3,DaysStandalone:4,MonthsFormat:5,MonthsStandalone:6,Eras:7,FirstDayOfWeek:8,WeekendRange:9,DateFormat:10,TimeFormat:11,DateTimeFormat:12,NumberSymbols:13,NumberFormats:14,CurrencyCode:15,CurrencySymbol:16,CurrencyName:17,Currencies:18,Directionality:19,PluralCase:20,ExtraData:21};function cN(t){return t.toLowerCase().replace(/_/g,`-`)}var nc=`en-US`;function $0(t){typeof t==`string`&&t.toLowerCase().replace(/_/g,`-`)}function C(t,n,e){let i=de(),a=Qe(),o=Dt();return dN(a,i,i[Je],o,t,n,e),C}function Dr(t,n,e){let i=de(),a=Qe(),o=Dt();return(o.type&3||e)&&Mf(o,a,i,e,i[Je],t,n,ho(o,i,n)),Dr}function dN(t,n,e,i,a,o,r){let s=!0,c=null;if((i.type&3||r)&&(c??=ho(i,n,o),Mf(i,t,n,r,e,a,o,c)&&(s=!1)),s){let m=i.outputs?.[a],u=i.hostDirectiveOutputs?.[a];if(u&&u.length)for(let g=0;g<u.length;g+=2){let v=u[g],b=u[g+1];c??=ho(i,n,o),Dd(i,n,v,b,a,c)}if(m&&m.length)for(let g of m)c??=ho(i,n,o),Dd(i,n,g,a,a,c)}}function _(t=1){return oy(t)}function uN(t,n){let e=null,i=Ok(t);for(let a=0;a<n.length;a++){let o=n[a];if(o===`*`){e=a;continue}if(i===null?U_(t,o,!0):Fk(i,o))return a}return e}function Oe(t){let n=de()[Kt][Ht];if(!n.projection){let i=n.projection=Av(t?t.length:1,null),a=i.slice(),o=n.child;for(;o!==null;){if(o.type!==128){let r=t?uN(o,t):0;r!==null&&(a[r]?a[r].projectionNext=o:i[r]=o,a[r]=o)}o=o.next}}}function ue(t,n=0,e,i,a,o){let r=de(),s=Qe(),c=i?t+1:null;c!==null&&vr(r,s,c,i,a,o,null,e);let m=vo(s,it+t,16,null,e||null);m.projection===null&&(m.projection=n),qp();let g=!r[no]||Hp();r[Kt][Ht].projection[m.projection]===null&&c!==null?mN(r,s,c):g&&!Pd(m)&&nI(s,r,m)}function mN(t,n,e){let i=it+e,a=n.data[i],o=t[i],r=wd(o,a.tView.ssrId);Js(o,Zs(t,a,void 0,{dehydratedView:r}),0,fr(a,r))}function Vt(t,n,e,i){return T0(t,n,e,i),Vt}function Xe(t,n,e){return N0(t,n,e),Xe}function X(t){let n=de(),e=Qe(),i=od();Rs(i+1);let a=Rf(e,i);if(t.dirty&&Uv(n)===((a.metadata.flags&2)===2)){if(a.matches===null)t.reset([]);else{let o=O0(n,i);t.reset(o,y_),t.notifyOnChanges()}return!0}return!1}function Z(){return Of(de(),od())}function $d(t,n,e,i,a){return P0(n,T0(t,e,i,a)),$d}function Gd(t,n,e,i){return P0(t,N0(n,e,i)),Gd}function Wd(t=1){Rs(od()+t)}function mt(t){return Ns(Yp(),it+t)}function hd(t,n){return t<<17|n<<2}function bo(t){return t>>17&32767}function pN(t){return(t&2)==2}function hN(t,n){return t&131071|n<<17}function Qh(t){return t|2}function yr(t){return(t&131068)>>2}function mh(t,n){return t&-131069|n<<2}function fN(t){return(t&1)===1}function Xh(t){return t|1}function gN(t,n,e,i,a,o){let r=o?n.classBindings:n.styleBindings,s=bo(r),c=yr(r);t[i]=e;let m=!1,u;if(Array.isArray(e)){let g=e;u=g[1],(u===null||er(g,u)>0)&&(m=!0)}else u=e;if(a)if(c!==0){let v=bo(t[s+1]);t[i+1]=hd(v,s),v!==0&&(t[v+1]=mh(t[v+1],i)),t[s+1]=hN(t[s+1],i)}else t[i+1]=hd(s,0),s!==0&&(t[s+1]=mh(t[s+1],i)),s=i;else t[i+1]=hd(c,0),s===0?s=i:t[c+1]=mh(t[c+1],i),c=i;m&&(t[i+1]=Qh(t[i+1])),Jy(t,u,i,!0),Jy(t,u,i,!1),bN(n,u,t,i,o),r=hd(s,c),o?n.classBindings=r:n.styleBindings=r}function bN(t,n,e,i,a){let o=a?t.residualClasses:t.residualStyles;o!=null&&typeof n==`string`&&er(o,n)>=0&&(e[i+1]=Xh(e[i+1]))}function Jy(t,n,e,i){let a=t[e+1],o=n===null,r=i?bo(a):yr(a),s=!1;for(;r!==0&&(s===!1||o);){let c=t[r],m=t[r+1];vN(c,n)&&(s=!0,t[r+1]=i?Xh(m):Qh(m)),r=i?bo(m):yr(m)}s&&(t[e+1]=i?Qh(a):Xh(a))}function vN(t,n){return t===null||n==null||(Array.isArray(t)?t[1]:t)===n?!0:Array.isArray(t)&&typeof n==`string`?er(t,n)>=0:!1}var Zn={textEnd:0,key:0,keyEnd:0,value:0,valueEnd:0};function yN(t){return t.substring(Zn.key,Zn.keyEnd)}function _N(t){return xN(t),G0(t,W0(t,0,Zn.textEnd))}function G0(t,n){let e=Zn.textEnd;return e===n?-1:(n=Zn.keyEnd=CN(t,Zn.key=n,e),W0(t,n,e))}function xN(t){Zn.key=0,Zn.keyEnd=0,Zn.value=0,Zn.valueEnd=0,Zn.textEnd=t.length}function W0(t,n,e){for(;n<e&&t.charCodeAt(n)<=32;)n++;return n}function CN(t,n,e){for(;n<e&&t.charCodeAt(n)>32;)n++;return n}function mn(t,n,e){return q0(t,n,e,!1),mn}function W(t,n){return q0(t,n,null,!0),W}function et(t){wN(NN,SN,t,!0)}function SN(t,n){for(let e=_N(n);e>=0;e=G0(n,e))Yl(t,yN(n),!0)}function q0(t,n,e,i){let a=de(),o=Qe(),r=uo(2);if(o.firstUpdatePass&&K0(o,t,r,i),n!==xt&&xn(a,r,n)){let s=o.data[dn()];Q0(o,s,a,a[Je],t,a[r+1]=AN(n,e),i,r)}}function wN(t,n,e,i){let a=Qe(),o=uo(2);a.firstUpdatePass&&K0(a,null,o,i);let r=de();if(e!==xt&&xn(r,o,e)){let s=a.data[dn()];if(X0(s,i)&&!Y0(a,o)){let c=i?s.classesWithoutHost:s.stylesWithoutHost;c!==null&&(e=Hl(c,e||``)),Kh(a,s,r,e,i)}else TN(a,s,r,r[Je],r[o+1],r[o+1]=MN(t,n,e),i,o)}}function Y0(t,n){return n>=t.expandoStartIndex}function K0(t,n,e,i){let a=t.data;if(a[e+1]===null){let o=a[dn()],r=Y0(t,e);X0(o,i)&&n===null&&!r&&(n=!1),n=EN(a,o,n,i),gN(a,o,n,e,r,i)}}function EN(t,n,e,i){let a=ty(t),o=i?n.residualClasses:n.residualStyles;if(a===null)(i?n.classBindings:n.styleBindings)===0&&(e=ph(null,t,n,e,i),e=Ws(e,n.attrs,i),o=null);else{let r=n.directiveStylingLast;if(r===-1||t[r]!==a)if(e=ph(a,t,n,e,i),o===null){let c=DN(t,n,i);c!==void 0&&Array.isArray(c)&&(c=ph(null,t,n,c[1],i),c=Ws(c,n.attrs,i),kN(t,n,i,c))}else o=IN(t,n,i)}return o!==void 0&&(i?n.residualClasses=o:n.residualStyles=o),e}function DN(t,n,e){let i=e?n.classBindings:n.styleBindings;if(yr(i)!==0)return t[bo(i)]}function kN(t,n,e,i){let a=e?n.classBindings:n.styleBindings;t[bo(a)]=i}function IN(t,n,e){let i,a=n.directiveEnd;for(let o=1+n.directiveStylingLast;o<a;o++){let r=t[o].hostAttrs;i=Ws(i,r,e)}return Ws(i,n.attrs,e)}function ph(t,n,e,i,a){let o=null,r=e.directiveEnd,s=e.directiveStylingLast;for(s===-1?s=e.directiveStart:s++;s<r&&(o=n[s],i=Ws(i,o.hostAttrs,a),o!==t);)s++;return t!==null&&(e.directiveStylingLast=s),i}function Ws(t,n,e){let i=e?1:2,a=-1;if(n!==null)for(let o=0;o<n.length;o++){let r=n[o];typeof r==`number`?a=r:a===i&&(Array.isArray(t)||(t=t===void 0?[]:[``,t]),Yl(t,r,e?!0:n[++o]))}return t===void 0?null:t}function MN(t,n,e){if(e==null||e===``)return Yt;let i=[],a=ei(e);if(Array.isArray(a))for(let o=0;o<a.length;o++)t(i,a[o],!0);else if(a instanceof Set)for(let o of a)t(i,o,!0);else if(typeof a==`object`)for(let o in a)Object.hasOwn(a,o)&&t(i,o,a[o]);else typeof a==`string`&&n(i,a);return i}function NN(t,n,e){let i=String(n);i!==``&&!i.includes(` `)&&Yl(t,i,e)}function TN(t,n,e,i,a,o,r,s){a===xt&&(a=Yt);let c=0,m=0,u=0<a.length?a[0]:null,g=0<o.length?o[0]:null;for(;u!==null||g!==null;){let v=c<a.length?a[c+1]:void 0,b=m<o.length?o[m+1]:void 0,y=null,E;u===g?(c+=2,m+=2,v!==b&&(y=g,E=b)):g===null||u!==null&&u<g?(c+=2,y=u):(m+=2,y=g,E=b),y!==null&&Q0(t,n,e,i,y,E,r,s),u=c<a.length?a[c]:null,g=m<o.length?o[m]:null}}function Q0(t,n,e,i,a,o,r,s){if(!(n.type&3))return;let c=t.data,m=c[s+1];if(!Rd(fN(m)?e_(c,n,e,a,yr(m),r):void 0)){Rd(o)||pN(m)&&(o=e_(c,null,e,a,s,r));aI(i,r,Fp(dn(),e),a,o)}}function e_(t,n,e,i,a,o){let r=n===null,s;for(;a>0;){let c=t[a],m=Array.isArray(c),u=m?c[1]:c,g=u===null,v=e[a+1];v===xt&&(v=g?Yt:void 0);let b=g?Kl(v,i):u===i?v:void 0;if(m&&!Rd(b)&&(b=Kl(c,i)),Rd(b)&&(s=b,r))return s;let y=t[a+1];a=r?bo(y):yr(y)}if(n!==null){let c=o?n.residualClasses:n.residualStyles;c!=null&&(s=Kl(c,i))}return s}function Rd(t){return t!==void 0}function AN(t,n){return t==null||t===``||(typeof n==`string`?t=ei(t)+n:typeof t==`object`&&(t=Cs(ei(t)))),t}function X0(t,n){return(t.flags&(n?8:16))!==0}function p(t,n=``){let e=de(),i=Qe(),a=t+it,o=i.firstCreatePass?vo(i,a,1,n,null):i.data[a],r=ON(i,e,o,n);e[a]=r,cd()&&vf(i,e,r,o),lo(o,!1)}var ON=(t,n,e,i)=>(Ps(!0),xk(n[Je],i));function RN(t,n,e,i=``){return xn(t,ga(),e)?n+Rt(e)+i:xt}function PN(t,n,e,i,a,o=``){let s=gr(t,Os(),e,a);return uo(2),s?n+Rt(e)+i+Rt(a)+o:xt}function FN(t,n,e,i,a,o,r,s=``){let m=WI(t,Os(),e,a,r);return uo(3),m?n+Rt(e)+i+Rt(a)+o+Rt(r)+s:xt}function LN(t,n,e,i,a,o,r,s,c,m=``){let g=x0(t,Os(),e,a,r,c);return uo(4),g?n+Rt(e)+i+Rt(a)+o+Rt(r)+s+Rt(c)+m:xt}function VN(t,n,e,i,a,o,r,s,c,m,u,g,v,b=``){let y=Os(),E=x0(t,y,e,a,r,c);return E=gr(t,y+4,u,v)||E,uo(6),E?n+Rt(e)+i+Rt(a)+o+Rt(r)+s+Rt(c)+m+Rt(u)+g+Rt(v)+b:xt}function w(t){return H(``,t),w}function H(t,n,e){let i=de(),a=RN(i,t,n,e);return a!==xt&&ic(i,dn(),a),H}function Ue(t,n,e,i,a){let o=de(),r=PN(o,t,n,e,i,a);return r!==xt&&ic(o,dn(),r),Ue}function Rn(t,n,e,i,a,o,r){let s=de(),c=FN(s,t,n,e,i,a,o,r);return c!==xt&&ic(s,dn(),c),Rn}function qi(t,n,e,i,a,o,r,s,c){let m=de(),u=LN(m,t,n,e,i,a,o,r,s,c);return u!==xt&&ic(m,dn(),u),qi}function qd(t,n,e,i,a,o,r,s,c,m,u,g,v){let b=de(),y=VN(b,t,n,e,i,a,o,r,s,c,m,u,g,v);return y!==xt&&ic(b,dn(),y),qd}function ic(t,n,e){let i=Fp(n,t);Ck(t[Je],i,e)}var Z0={};function Yd(t){Ci(`NgLet`);let n=Qe(),e=de(),i=t+it;return lo(vo(n,i,128,null,null),!1),Ts(n,e,i,Z0),Yd}function qf(t){return Ts(Qe(),de(),dn(),t),t}function Yf(t){let e=Ns(Yp(),it+t);if(e===Z0)throw new $(314,!1);return e}function t_(t,n,e){let i=Qe();i.firstCreatePass&&J0(n,i.data,i.blueprint,Qn(t),e)}function J0(t,n,e,i,a){if(t=Ot(t),Array.isArray(t))for(let o=0;o<t.length;o++)J0(t[o],n,e,i,a);else{let o=Qe(),r=de(),s=Dt(),c=Za(t)?t:Ot(t.provide),m=Np(t),u=s.providerIndexes&1048575,g=s.directiveStart,v=s.providerIndexes>>20;if(Za(t)||!t.multi){let b=new fo(m,a,je,null),y=fh(c,n,a?u:u+v,g);y===-1?(vh(Sd(s,r),o,c),hh(o,t,n.length),n.push(c),s.directiveStart++,s.directiveEnd++,a&&(s.providerIndexes+=1048576),e.push(b),r.push(b)):(e[y]=b,r[y]=b)}else{let b=fh(c,n,u+v,g),y=fh(c,n,u,u+v),E=b>=0&&e[b],R=y>=0&&e[y];if(a&&!R||!a&&!E){vh(Sd(s,r),o,c);let oe=zN(a?jN:BN,e.length,a,i,m,t);!a&&R&&(e[y].providerFactory=oe),hh(o,t,n.length,0),n.push(c),s.directiveStart++,s.directiveEnd++,a&&(s.providerIndexes+=1048576),e.push(oe),r.push(oe)}else{let oe=ex(e[a?y:b],m,!a&&i);hh(o,t,b>-1?b:y,oe)}!a&&i&&R&&e[y].componentProviders++}}}function hh(t,n,e,i){let a=Za(n),o=Vv(n);if(a||o){let c=(o?Ot(n.useClass):n).prototype.ngOnDestroy;if(c){let m=t.destroyHooks||(t.destroyHooks=[]);if(!a&&n.multi){let u=m.indexOf(e);u===-1?m.push(e,[i,c]):m[u+1].push(i,c)}else m.push(e,c)}}}function ex(t,n,e){return e&&t.componentProviders++,t.multi.push(n)-1}function fh(t,n,e,i){for(let a=e;a<i;a++)if(n[a]===t)return a;return-1}function BN(t,n,e,i,a){return Zh(this.multi,[])}function jN(t,n,e,i,a){let o=this.multi,r;if(this.providerFactory){let s=this.providerFactory.componentProviders,c=zs(i,i[fe],this.providerFactory.index,a);r=c.slice(0,s),Zh(o,r);for(let m=s;m<c.length;m++)r.push(c[m])}else r=[],Zh(o,r);return r}function Zh(t,n){for(let e=0;e<t.length;e++){let i=t[e];n.push(i())}return n}function zN(t,n,e,i,a,o){let r=new fo(t,e,je,null);return r.multi=[],r.index=n,r.componentProviders=0,ex(r,a,i&&!e),r}function $e(t,n){return e=>{e.providersResolver=(i,a)=>t_(i,a?a(t):t,!1),n&&(e.viewProvidersResolver=(i,a)=>t_(i,a?a(n):n,!0))}}function Bt(t,n){let e=id()+t,i=de();return i[e]===xt?If(i,e,n()):GI(i,e)}function tx(t,n){let e=t[n];return e===xt?void 0:e}function HN(t,n,e,i,a,o){let r=n+e;return xn(t,r,a)?If(t,r+1,o?i.call(o,a):i(a)):tx(t,r+1)}function UN(t,n,e,i,a,o,r){let s=n+e;return gr(t,s,a,o)?If(t,s+2,r?i.call(r,a,o):i(a,o)):tx(t,s+2)}function Q(t,n){let e=Qe(),i,a=t+it;e.firstCreatePass?(i=$N(n,e.pipeRegistry),e.data[a]=i,i.onDestroy&&(e.destroyHooks??=[]).push(a,i.onDestroy)):i=e.data[a];let o=i.factory||(i.factory=ua(i.type,!0)),s=nn(je);try{let c=Cd(!1),m=o();return Cd(c),Ts(e,de(),a,m),m}finally{nn(s)}}function $N(t,n){if(n)for(let e=n.length-1;e>=0;e--){let i=n[e];if(t===i.name)return i}}function Ce(t,n,e){let i=t+it,a=de(),o=Ns(a,i);return nx(a,i)?HN(a,id(),n,o.transform,e,o):o.transform(e)}function He(t,n,e,i){let a=t+it,o=de(),r=Ns(o,a);return nx(o,a)?UN(o,id(),n,r.transform,e,i,r):r.transform(e,i)}function nx(t,n){return t[fe].data[n].pure}function ac(t,n){return Ud(t,n)}var ix=(()=>{class t{applicationErrorHandler=f(zi);appRef=f(On);taskService=f(mo);ngZone=f(K);zonelessEnabled=f(Ls);tracing=f(ni,{optional:!0});zoneIsDefined=typeof Zone<`u`&&!!Zone.root.run;schedulerTickApplyArgs=[{data:{__scheduler_tick__:!0}}];subscriptions=new De;angularZoneId=this.zoneIsDefined?this.ngZone._inner?.get(_s):null;scheduleInRootZone=!this.zonelessEnabled&&this.zoneIsDefined&&(f(ah,{optional:!0})??!1);cancelScheduledCallback=null;useMicrotaskScheduler=!1;runningTick=!1;pendingRenderTaskId=null;constructor(){this.subscriptions.add(this.appRef.afterTick.subscribe(()=>{let e=this.taskService.add();if(!this.runningTick&&(this.cleanup(),!this.zonelessEnabled||this.appRef.includeAllTestViews)){this.taskService.remove(e);return}this.switchToMicrotaskScheduler(),this.taskService.remove(e)})),this.subscriptions.add(this.ngZone.onUnstable.subscribe(()=>{this.runningTick||this.cleanup()}))}switchToMicrotaskScheduler(){this.ngZone.runOutsideAngular(()=>{let e=this.taskService.add();this.useMicrotaskScheduler=!0,queueMicrotask(()=>{this.useMicrotaskScheduler=!1,this.taskService.remove(e)})})}notify(e){if(!this.zonelessEnabled&&e===5)return;switch(e){case 0:case 2:this.appRef.dirtyFlags|=2;break;case 3:case 4:case 5:case 1:this.appRef.dirtyFlags|=4;break;case 6:this.appRef.dirtyFlags|=2;break;case 12:this.appRef.dirtyFlags|=16;break;case 13:this.appRef.dirtyFlags|=2;break;case 11:break;default:this.appRef.dirtyFlags|=8}if(this.appRef.tracingSnapshot=this.tracing?.snapshot(this.appRef.tracingSnapshot)??null,!this.shouldScheduleTick())return;let i=this.useMicrotaskScheduler?ly:eh;this.pendingRenderTaskId=this.taskService.add(),this.scheduleInRootZone?this.cancelScheduledCallback=Zone.root.run(()=>i(()=>this.tick())):this.cancelScheduledCallback=this.ngZone.runOutsideAngular(()=>i(()=>this.tick()))}shouldScheduleTick(){return!(this.appRef.destroyed||this.pendingRenderTaskId!==null||this.runningTick||this.appRef._runningTick||!this.zonelessEnabled&&this.zoneIsDefined&&Zone.current.get(_s+this.angularZoneId))}tick(){if(this.runningTick||this.appRef.destroyed)return;if(this.appRef.dirtyFlags===0){this.cleanup();return}!this.zonelessEnabled&&this.appRef.dirtyFlags&7&&(this.appRef.dirtyFlags|=1);let e=this.taskService.add();try{this.ngZone.run(()=>{this.runningTick=!0,this.appRef._tick()},void 0,this.schedulerTickApplyArgs)}catch(i){this.applicationErrorHandler(i)}finally{this.taskService.remove(e),this.cleanup()}}ngOnDestroy(){this.subscriptions.unsubscribe(),this.cleanup()}cleanup(){if(this.runningTick=!1,this.cancelScheduledCallback?.(),this.cancelScheduledCallback=null,this.pendingRenderTaskId!==null){let e=this.pendingRenderTaskId;this.pendingRenderTaskId=null,this.taskService.remove(e)}}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();function Kf(){return Ci(`NgZoneless`),an([...Qf(),[]])}function Qf(){return[{provide:gi,useExisting:ix},{provide:K,useClass:xs},{provide:Ls,useValue:!0}]}function GN(){return typeof $localize<`u`&&$localize.locale||nc}var kr=new D(``,{factory:()=>f(kr,{optional:!0,skipSelf:!0})||GN()});var oc=class{destroyed=!1;listeners=null;errorHandler=f(Mn,{optional:!0});isEmitting=!1;hasNullListeners=!1;destroyRef=f(Ne);constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.listeners=null})}subscribe(n){if(this.destroyed)throw new $(953,!1);return(this.listeners??=[]).push(n),{unsubscribe:()=>{let e=this.listeners?this.listeners.indexOf(n):-1;e>-1&&(this.isEmitting?(this.hasNullListeners=!0,this.listeners[e]=null):this.listeners.splice(e,1))}}}emit(n){if(this.destroyed){console.warn(Vi(953,!1));return}if(this.listeners===null)return;this.isEmitting=!0;let e=pe(null);try{for(let i of this.listeners)try{i!==null&&i(n)}catch(a){this.errorHandler?.handleError(a)}}finally{this.hasNullListeners&&(this.hasNullListeners=!1,this.listeners&&WN(this.listeners)),pe(e),this.isEmitting=!1}}};function WN(t){let n=t.length-1;for(;n>-1;)t[n]===null&&t.splice(n,1),n--}function ne(t,n){return us(t,n?.equal)}function tt(t){return Gb(t)}(class t extends Error{_brand;constructor(n){super(n)}static IDLE=new t(`IDLE`);static LOADING=new t(`LOADING`)});function ux(t,n){let e=Object.create(Jh);e.value=t,e.transformFn=n?.transform;function i(){if(Ai(e),e.value===_r)throw new $(-950,null);return e.value}return i[yt]=e,i}var Yi=class{attributeName;constructor(n){this.attributeName=n}__NG_ELEMENT_ID__=()=>tf(this.attributeName);toString(){return`HostAttributeToken ${this.attributeName}`}};var mx=(()=>{let t=new D(``);return t.__NG_ELEMENT_ID__=n=>{let e=Dt();if(e===null)throw new $(-204,!1);if(e.type&2)return e.value;if(n&8)return null;throw new $(-204,!1)},t})();function Qd(t){return new oc}function ox(t,n){return ux(t,n)}function r1(t){return ux(_r,t)}var jt=(ox.required=r1,ox);function px(t,n){let e=Object.create(Jh),i=new oc;e.value=t;function a(){return Ai(e),rx(e.value),e.value}return a[yt]=e,a.asReadonly=ld.bind(a),a.set=o=>{e.equal(e.value,o)||(da(e,o),i.emit(o))},a.update=o=>{rx(e.value),a.set(o(e.value))},a.subscribe=i.subscribe.bind(i),a.destroyRef=i.destroyRef,a}function rx(t){if(t===_r)throw new $(952,!1)}function sx(t,n){return px(t,n)}function s1(t){return px(_r,t)}var hx=(sx.required=s1,sx);function cx(t,n){return Pf(n)}function c1(t,n){return Ff(n)}var sc=(cx.required=c1,cx);function lx(t,n){return Pf(n)}function l1(t,n){return Ff(n)}var fx=(lx.required=l1,lx);var Re=(()=>{class t{static __NG_ELEMENT_ID__=u1}return t})();function u1(t){return m1(Dt(),de(),(t&16)===16)}function m1(t,n,e){if(yi(t)&&!e){let i=An(t.index,n);return new xa(i,i)}else if(t.type&175){let i=n[Kt];return new xa(i,n)}return null}var Zf=new D(``);var p1=new D(``);function rc(t){return!t.moduleRef}function h1(t){let n=rc(t)?t.r3Injector:t.moduleRef.injector,e=n.get(K);return e.run(()=>{rc(t)?t.r3Injector.resolveInjectorInitializers():t.moduleRef.resolveInjectorInitializers();let i=n.get(zi),a;if(e.runOutsideAngular(()=>{a=e.onError.subscribe({next:i})}),rc(t)){let o=()=>n.destroy(),r=t.platformInjector.get(Zf);r.add(o),n.onDestroy(()=>{a.unsubscribe(),r.delete(o)})}else{let o=()=>t.moduleRef.destroy(),r=t.platformInjector.get(Zf);r.add(o),t.moduleRef.onDestroy(()=>{js(t.allPlatformModules,t.moduleRef),a.unsubscribe(),r.delete(o)})}return g1(i,e,()=>{let o=n.get(mo),r=o.add(),s=n.get(Bf);return s.runInitializers(),s.donePromise.then(()=>{if($0(n.get(kr,nc)||nc),!n.get(p1,!0))return rc(t)?n.get(On):(t.allPlatformModules.push(t.moduleRef),t.moduleRef);if(rc(t)){let u=n.get(On);return t.rootComponent!==void 0&&u.bootstrap(t.rootComponent),u}else return f1?.(t.moduleRef,t.allPlatformModules),t.moduleRef}).finally(()=>{o.remove(r)})})})}var f1;function g1(t,n,e){try{let i=e();return _o(i)?i.catch(a=>{throw n.runOutsideAngular(()=>t(a)),a}):i}catch(i){throw n.runOutsideAngular(()=>t(i)),i}}var Kd=null;function b1(t=[],n){return re.create({name:n,providers:[{provide:ks,useValue:`platform`},{provide:Zf,useValue:new Set([()=>Kd=null])},...t]})}function v1(t=[]){if(Kd)return Kd;let n=b1(t);return Kd=n,H0(),y1(n),n}function y1(t){let n=t.get(dd,null);nr(t,()=>{n?.forEach(e=>e())})}function gx(t){let{rootComponent:n,appProviders:e,platformProviders:i,platformRef:a}=t;Ye(Be.BootstrapApplicationStart);try{let o=a?.injector??v1(i);return h1({r3Injector:new Gs({providers:[Qf(),py,...e||[]],parent:o,debugName:``,runEnvironmentInitializers:!1}).injector,platformInjector:o,rootComponent:n})}catch(o){return Promise.reject(o)}finally{Ye(Be.BootstrapApplicationEnd)}}function V(t){return typeof t==`boolean`?t:t!=null&&t!==`false`}function st(t,n=NaN){return!isNaN(parseFloat(t))&&!isNaN(Number(t))?Number(t):n}var Xf=Symbol(`NOT_SET`);var bx=new Set;var _1=s(r({},Uo),{kind:`afterRenderEffectPhase`,consumerIsAlwaysLive:!0,consumerAllowSignalWrites:!0,value:Xf,cleanup:null,consumerMarkedDirty(){if(this.sequence.impl.executing){if(this.sequence.lastPhase===null||this.sequence.lastPhase<this.phase)return;this.sequence.erroredOrDestroyed=!0}this.sequence.scheduler.notify(7)},phaseFn(t){if(this.sequence.lastPhase=this.phase,!this.dirty)return this.signal;if(this.dirty=!1,this.value!==Xf&&!zo(this))return this.signal;try{for(let a of this.cleanup??bx)a()}finally{this.cleanup?.clear()}let n=[];t!==void 0&&n.push(t),n.push(this.registerCleanupFn);let e=ca(this),i;try{i=this.userFn.apply(null,n)}finally{Ua(this,e)}return(this.value===Xf||!this.equal(this.value,i))&&(this.value=i,this.version++),this.signal}});var Jf=class extends Hs{scheduler;lastPhase=null;nodes=[void 0,void 0,void 0,void 0];onDestroyFns=null;constructor(n,e,i,a,o,r=null,s){super(n,[void 0,void 0,void 0,void 0],i,!1,o.get(Ne),r),this.scheduler=a;for(let c of ff){let m=e[c];if(m===void 0)continue;let u=Object.create(_1);u.sequence=this,u.phase=c,u.userFn=m,u.dirty=!0,u.signal=()=>(Ai(u),u.value),u.signal[yt]=u,u.registerCleanupFn=g=>(u.cleanup??=new Set).add(g),this.nodes[c]=u,this.hooks[c]=g=>u.phaseFn(g)}}afterRun(){super.afterRun(),this.lastPhase=null}destroy(){if(this.onDestroyFns!==null)for(let n of this.onDestroyFns)n();super.destroy();for(let n of this.nodes)if(n)try{for(let e of n.cleanup??bx)e()}finally{la(n)}}};function eg(t,n){let e=n?.injector??f(re),i=e.get(gi),a=e.get(Vd),o=e.get(ni,null,{optional:!0});a.impl??=e.get(gf);let r=t;typeof r==`function`&&(r={mixedReadWrite:t});let s=e.get(lr,null,{optional:!0}),c=new Jf(a.impl,[r.earlyRead,r.write,r.mixedReadWrite,r.read],s?.view,i,e,o?.snapshot(null),void 0);return a.impl.register(c),c}function Xd(t,n){let e=ma(t),i=n.elementInjector||tr(),o=new br(e).create(i,n.projectableNodes,n.hostElement,n.environmentInjector,n.directives,n.bindings);return n.onError&&(o.hostView._lView[or]=n.onError),o}var vx=null;function Pn(){return vx}function tg(t){vx??=t}var cc=class{};var Ir=(()=>{class t{historyGo(e){throw new Error(``)}static ɵfac=function(i){return new(i||t)};static ɵprov=he({token:t,factory:()=>f(yx),providedIn:`platform`})}return t})();var yx=(()=>{class t extends Ir{_location;_history;_doc=f(ce);constructor(){super(),this._location=window.location,this._history=window.history}getBaseHrefFromDOM(){return Pn().getBaseHref(this._doc)}onPopState(e){let i=Pn().getGlobalEventTarget(this._doc,`window`);return i.addEventListener(`popstate`,e,!1),()=>i.removeEventListener(`popstate`,e)}onHashChange(e){let i=Pn().getGlobalEventTarget(this._doc,`window`);return i.addEventListener(`hashchange`,e,!1),()=>i.removeEventListener(`hashchange`,e)}get href(){return this._location.href}get protocol(){return this._location.protocol}get hostname(){return this._location.hostname}get port(){return this._location.port}get pathname(){return this._location.pathname}get search(){return this._location.search}get hash(){return this._location.hash}set pathname(e){this._location.pathname=e}pushState(e,i,a){this._history.pushState(e,i,a)}replaceState(e,i,a){this._history.replaceState(e,i,a)}forward(){this._history.forward()}back(){this._history.back()}historyGo(e=0){this._history.go(e)}getState(){return this._history.state}static ɵfac=function(i){return new(i||t)};static ɵprov=he({token:t,factory:()=>new t,providedIn:`platform`})}return t})();function Cx(t,n){return t?n?t.endsWith(`/`)?n.startsWith(`/`)?t+n.slice(1):t+n:n.startsWith(`/`)?t+n:`${t}/${n}`:t:n}function _x(t){let n=t.search(/#|\?|$/);return t[n-1]===`/`?t.slice(0,n-1)+t.slice(n):t}function wa(t){return t&&t[0]!==`?`?`?${t}`:t}var Zd=(()=>{class t{historyGo(e){throw new Error(``)}static ɵfac=function(i){return new(i||t)};static ɵprov=he({token:t,factory:()=>f(C1),providedIn:`root`})}return t})();var x1=new D(``);var C1=(()=>{class t extends Zd{_platformLocation;_baseHref;_removeListenerFns=[];constructor(e,i){super(),this._platformLocation=e,this._baseHref=i??this._platformLocation.getBaseHrefFromDOM()??f(ce).location?.origin??``}ngOnDestroy(){for(;this._removeListenerFns.length;)this._removeListenerFns.pop()()}onPopState(e){this._removeListenerFns.push(this._platformLocation.onPopState(e),this._platformLocation.onHashChange(e))}getBaseHref(){return this._baseHref}prepareExternalUrl(e){return Cx(this._baseHref,e)}path(e=!1){let i=this._platformLocation.pathname+wa(this._platformLocation.search),a=this._platformLocation.hash;return a&&e?`${i}${a}`:i}pushState(e,i,a,o){let r=this.prepareExternalUrl(a+wa(o));this._platformLocation.pushState(e,i,r)}replaceState(e,i,a,o){let r=this.prepareExternalUrl(a+wa(o));this._platformLocation.replaceState(e,i,r)}forward(){this._platformLocation.forward()}back(){this._platformLocation.back()}getState(){return this._platformLocation.getState()}historyGo(e=0){this._platformLocation.historyGo?.(e)}static ɵfac=function(i){return new(i||t)(Se(Ir),Se(x1,8))};static ɵprov=he({token:t,factory:t.ɵfac,providedIn:`root`})}return t})();var Jd=(()=>{class t{_subject=new L;_basePath;_locationStrategy;_urlChangeListeners=[];_urlChangeSubscription=null;constructor(e){this._locationStrategy=e;let i=this._locationStrategy.getBaseHref();this._basePath=E1(_x(xx(i))),this._locationStrategy.onPopState(a=>{let o={url:this.path(!0),pop:!0,state:a.state,type:a.type};a.hasUAVisualTransition&&(o.hasUAVisualTransition=!0),this._subject.next(o)})}ngOnDestroy(){this._urlChangeSubscription?.unsubscribe(),this._urlChangeListeners=[]}path(e=!1){return this.normalize(this._locationStrategy.path(e))}getState(){return this._locationStrategy.getState()}isCurrentPathEqualTo(e,i=``){return this.path()==this.normalize(e+wa(i))}normalize(e){return t.stripTrailingSlash(w1(this._basePath,xx(e)))}prepareExternalUrl(e){return e&&e[0]!==`/`&&(e=`/`+e),this._locationStrategy.prepareExternalUrl(e)}go(e,i=``,a=null){this._locationStrategy.pushState(a,``,e,i),this._notifyUrlChangeListeners(this.prepareExternalUrl(e+wa(i)),a)}replaceState(e,i=``,a=null){this._locationStrategy.replaceState(a,``,e,i),this._notifyUrlChangeListeners(this.prepareExternalUrl(e+wa(i)),a)}forward(){this._locationStrategy.forward()}back(){this._locationStrategy.back()}historyGo(e=0){this._locationStrategy.historyGo?.(e)}onUrlChange(e){return this._urlChangeListeners.push(e),this._urlChangeSubscription??=this.subscribe(i=>{this._notifyUrlChangeListeners(i.url,i.state)}),()=>{let i=this._urlChangeListeners.indexOf(e);this._urlChangeListeners.splice(i,1),this._urlChangeListeners.length===0&&(this._urlChangeSubscription?.unsubscribe(),this._urlChangeSubscription=null)}}_notifyUrlChangeListeners(e=``,i){this._urlChangeListeners.forEach(a=>a(e,i))}subscribe(e,i,a){return this._subject.subscribe({next:e,error:i??void 0,complete:a??void 0})}static normalizeQueryParams=wa;static joinWithSlash=Cx;static stripTrailingSlash=_x;static ɵfac=function(i){return new(i||t)(Se(Zd))};static ɵprov=he({token:t,factory:()=>S1(),providedIn:`root`})}return t})();function S1(){return new Jd(Se(Zd))}function w1(t,n){if(!t||!n.startsWith(t))return n;let e=n.substring(t.length);return e===``||[`/`,`;`,`?`,`#`].includes(e[0])?e:n}function xx(t){return t.replace(/\/index\.html$/,``)}function E1(t){if(new RegExp(`^(https?:)?//`).test(t)){let[,e]=t.split(/\/\/[^\/]+/);return e}return t}var sg=(function(t){return t[t.Decimal=0]=`Decimal`,t[t.Percent=1]=`Percent`,t[t.Currency=2]=`Currency`,t[t.Scientific=3]=`Scientific`,t})(sg||{});var Qt=(function(t){return t[t.Format=0]=`Format`,t[t.Standalone=1]=`Standalone`,t})(Qt||{});var nt=(function(t){return t[t.Narrow=0]=`Narrow`,t[t.Abbreviated=1]=`Abbreviated`,t[t.Wide=2]=`Wide`,t[t.Short=3]=`Short`,t})(nt||{});var pn=(function(t){return t[t.Short=0]=`Short`,t[t.Medium=1]=`Medium`,t[t.Long=2]=`Long`,t[t.Full=3]=`Full`,t})(pn||{});var rn={Decimal:0,Group:1,List:2,PercentSign:3,PlusSign:4,MinusSign:5,Exponential:6,SuperscriptingExponent:7,PerMille:8,Infinity:9,NaN:10,TimeSeparator:11,CurrencyDecimal:12,CurrencyGroup:13};function Ex(t){return un(t)[Ct.LocaleId]}function Dx(t,n,e){let i=un(t);return Fn(Fn([i[Ct.DayPeriodsFormat],i[Ct.DayPeriodsStandalone]],n),e)}function kx(t,n,e){let i=un(t);return Fn(Fn([i[Ct.DaysFormat],i[Ct.DaysStandalone]],n),e)}function Ix(t,n,e){let i=un(t);return Fn(Fn([i[Ct.MonthsFormat],i[Ct.MonthsStandalone]],n),e)}function Mx(t,n){let i=un(t)[Ct.Eras];return Fn(i,n)}function lc(t,n){return Fn(un(t)[Ct.DateFormat],n)}function dc(t,n){return Fn(un(t)[Ct.TimeFormat],n)}function uc(t,n){let i=un(t)[Ct.DateTimeFormat];return Fn(i,n)}function Si(t,n){let e=un(t),i=e[Ct.NumberSymbols][n];if(typeof i>`u`){if(n===rn.CurrencyDecimal)return e[Ct.NumberSymbols][rn.Decimal];if(n===rn.CurrencyGroup)return e[Ct.NumberSymbols][rn.Group]}return i}function Nx(t,n){return un(t)[Ct.NumberFormats][n]}function Tx(t){if(!t[Ct.ExtraData])throw new $(2303,!1)}function Ax(t){let n=un(t);return Tx(n),(n[Ct.ExtraData][2]||[]).map(i=>typeof i==`string`?ng(i):[ng(i[0]),ng(i[1])])}function Ox(t,n,e){let i=un(t);Tx(i);return Fn(Fn([i[Ct.ExtraData][0],i[Ct.ExtraData][1]],n)||[],e)||[]}function Fn(t,n){for(let e=n;e>-1;e--)if(typeof t[e]<`u`)return t[e];throw new $(2304,!1)}function ng(t){let[n,e]=t.split(`:`);return{hours:+n,minutes:+e}}var k1=/^(\d{4,})-?(\d\d)-?(\d\d)(?:T(\d\d)(?::?(\d\d)(?::?(\d\d)(?:\.(\d+))?)?)?(Z|([+-])(\d\d):?(\d\d))?)?$/;var eu=Object.create(null);var I1=/((?:[^BEGHLMOSWYZabcdhmswyz']+)|(?:'(?:[^']|'')*')|(?:G{1,5}|y{1,4}|Y{1,4}|M{1,5}|L{1,5}|w{1,2}|W{1}|d{1,2}|E{1,6}|c{1,6}|a{1,5}|b{1,5}|B{1,5}|h{1,2}|H{1,2}|m{1,2}|s{1,2}|S{1,3}|z{1,4}|Z{1,5}|O{1,4}))([\s\S]*)/;var M1=256;function Rx(t,n,e,i){let a=B1(t);N1(n),n=Ki(e,n)||n;let r=[],s;for(;n;)if(s=I1.exec(n),s){r=r.concat(s.slice(1));let u=r.pop();if(!u)break;n=u}else{r.push(n);break}let c=a.getTimezoneOffset();i&&(c=Fx(i,c),a=V1(a,i));let m=``;return r.forEach(u=>{let g=F1(u);m+=g?g(a,e,c):u===`''`?`'`:u.replace(/(^'|'$)/g,``).replace(/''/g,`'`)}),m}function N1(t){if(t.length>M1)throw new $(2300,!1)}function ou(t,n,e){let i=new Date(0);return i.setFullYear(t,n,e),i.setHours(0,0,0),i}function Ki(t,n){let e=Ex(t);if(eu[e]??=Object.create(null),eu[e][n])return eu[e][n];let i=``;switch(n){case`shortDate`:i=lc(t,pn.Short);break;case`mediumDate`:i=lc(t,pn.Medium);break;case`longDate`:i=lc(t,pn.Long);break;case`fullDate`:i=lc(t,pn.Full);break;case`shortTime`:i=dc(t,pn.Short);break;case`mediumTime`:i=dc(t,pn.Medium);break;case`longTime`:i=dc(t,pn.Long);break;case`fullTime`:i=dc(t,pn.Full);break;case`short`:let a=Ki(t,`shortTime`),o=Ki(t,`shortDate`);i=tu(uc(t,pn.Short),[a,o]);break;case`medium`:let r=Ki(t,`mediumTime`),s=Ki(t,`mediumDate`);i=tu(uc(t,pn.Medium),[r,s]);break;case`long`:let c=Ki(t,`longTime`),m=Ki(t,`longDate`);i=tu(uc(t,pn.Long),[c,m]);break;case`full`:let u=Ki(t,`fullTime`),g=Ki(t,`fullDate`);i=tu(uc(t,pn.Full),[u,g])}return i&&(eu[e][n]=i),i}function tu(t,n){return n&&(t=t.replace(/\{([^}]+)}/g,function(e,i){return Object.hasOwn(n,i)?n[i]:e})),t}function oi(t,n,e=`-`,i,a){let o=``;(t<0||a&&t<=0)&&(a?t=-t+1:(t=-t,o=e));let r=String(t);for(;r.length<n;)r=`0`+r;return i&&(r=r.slice(r.length-n)),o+r}function T1(t,n){return oi(t,3).substring(0,n)}function Nt(t,n,e=0,i=!1,a=!1){return function(o,r){let s=A1(t,o);if((e>0||s>-e)&&(s+=e),t===3)s===0&&e===-12&&(s=12);else if(t===6)return T1(s,n);let c=Si(r,rn.MinusSign);return oi(s,n,c,i,a)}}function A1(t,n){switch(t){case 0:return n.getFullYear();case 1:return n.getMonth();case 2:return n.getDate();case 3:return n.getHours();case 4:return n.getMinutes();case 5:return n.getSeconds();case 6:return n.getMilliseconds();case 7:return n.getDay();default:throw new $(2301,!1)}}function ct(t,n,e=Qt.Format,i=!1){return function(a,o){return O1(a,o,t,n,e,i)}}function O1(t,n,e,i,a,o){switch(e){case 2:return Ix(n,a,i)[t.getMonth()];case 1:return kx(n,a,i)[t.getDay()];case 0:let r=t.getHours(),s=t.getMinutes();if(o){let m=Ax(n),u=Ox(n,a,i),g=m.findIndex(v=>{if(Array.isArray(v)){let[b,y]=v,E=r>=b.hours&&s>=b.minutes,R=r<y.hours||r===y.hours&&s<y.minutes;if(b.hours<y.hours){if(E&&R)return!0}else if(E||R)return!0}else if(v.hours===r&&v.minutes===s)return!0;return!1});if(g!==-1)return u[g]}return Dx(n,a,i)[r<12?0:1];case 3:return Mx(n,i)[t.getFullYear()<=0?0:1];default:throw new $(2302,!1)}}function nu(t){return function(n,e,i){let a=-1*i,o=Si(e,rn.MinusSign),r=a>0?Math.floor(a/60):Math.ceil(a/60);switch(t){case 0:return(a>=0?`+`:``)+oi(r,2,o)+oi(Math.abs(a%60),2,o);case 1:return`GMT`+(a>=0?`+`:``)+oi(r,1,o);case 2:return`GMT`+(a>=0?`+`:``)+oi(r,2,o)+`:`+oi(Math.abs(a%60),2,o);case 3:return i===0?`Z`:(a>=0?`+`:``)+oi(r,2,o)+`:`+oi(Math.abs(a%60),2,o);default:throw new $(2310,!1)}}}var R1=0;var au=4;function P1(t){let n=ou(t,R1,1).getDay();return ou(t,0,1+(n<=au?au:au+7)-n)}function Px(t){let n=t.getDay(),e=n===0?-3:au-n;return ou(t.getFullYear(),t.getMonth(),t.getDate()+e)}function ig(t,n=!1){return function(e,i){let a;if(n){let o=new Date(e.getFullYear(),e.getMonth(),1).getDay()-1,r=e.getDate();a=1+Math.floor((r+o)/7)}else{let o=Px(e),r=P1(o.getFullYear()),s=o.getTime()-r.getTime();a=1+Math.round(s/6048e5)}return oi(a,t,Si(i,rn.MinusSign))}}function iu(t,n=!1){return function(e,i){return oi(Px(e).getFullYear(),t,Si(i,rn.MinusSign),n)}}var ag=Object.create(null);function F1(t){if(ag[t])return ag[t];let n;switch(t){case`G`:case`GG`:case`GGG`:n=ct(3,nt.Abbreviated);break;case`GGGG`:n=ct(3,nt.Wide);break;case`GGGGG`:n=ct(3,nt.Narrow);break;case`y`:n=Nt(0,1,0,!1,!0);break;case`yy`:n=Nt(0,2,0,!0,!0);break;case`yyy`:n=Nt(0,3,0,!1,!0);break;case`yyyy`:n=Nt(0,4,0,!1,!0);break;case`Y`:n=iu(1);break;case`YY`:n=iu(2,!0);break;case`YYY`:n=iu(3);break;case`YYYY`:n=iu(4);break;case`M`:case`L`:n=Nt(1,1,1);break;case`MM`:case`LL`:n=Nt(1,2,1);break;case`MMM`:n=ct(2,nt.Abbreviated);break;case`MMMM`:n=ct(2,nt.Wide);break;case`MMMMM`:n=ct(2,nt.Narrow);break;case`LLL`:n=ct(2,nt.Abbreviated,Qt.Standalone);break;case`LLLL`:n=ct(2,nt.Wide,Qt.Standalone);break;case`LLLLL`:n=ct(2,nt.Narrow,Qt.Standalone);break;case`w`:n=ig(1);break;case`ww`:n=ig(2);break;case`W`:n=ig(1,!0);break;case`d`:n=Nt(2,1);break;case`dd`:n=Nt(2,2);break;case`c`:case`cc`:n=Nt(7,1);break;case`ccc`:n=ct(1,nt.Abbreviated,Qt.Standalone);break;case`cccc`:n=ct(1,nt.Wide,Qt.Standalone);break;case`ccccc`:n=ct(1,nt.Narrow,Qt.Standalone);break;case`cccccc`:n=ct(1,nt.Short,Qt.Standalone);break;case`E`:case`EE`:case`EEE`:n=ct(1,nt.Abbreviated);break;case`EEEE`:n=ct(1,nt.Wide);break;case`EEEEE`:n=ct(1,nt.Narrow);break;case`EEEEEE`:n=ct(1,nt.Short);break;case`a`:case`aa`:case`aaa`:n=ct(0,nt.Abbreviated);break;case`aaaa`:n=ct(0,nt.Wide);break;case`aaaaa`:n=ct(0,nt.Narrow);break;case`b`:case`bb`:case`bbb`:n=ct(0,nt.Abbreviated,Qt.Standalone,!0);break;case`bbbb`:n=ct(0,nt.Wide,Qt.Standalone,!0);break;case`bbbbb`:n=ct(0,nt.Narrow,Qt.Standalone,!0);break;case`B`:case`BB`:case`BBB`:n=ct(0,nt.Abbreviated,Qt.Format,!0);break;case`BBBB`:n=ct(0,nt.Wide,Qt.Format,!0);break;case`BBBBB`:n=ct(0,nt.Narrow,Qt.Format,!0);break;case`h`:n=Nt(3,1,-12);break;case`hh`:n=Nt(3,2,-12);break;case`H`:n=Nt(3,1);break;case`HH`:n=Nt(3,2);break;case`m`:n=Nt(4,1);break;case`mm`:n=Nt(4,2);break;case`s`:n=Nt(5,1);break;case`ss`:n=Nt(5,2);break;case`S`:n=Nt(6,1);break;case`SS`:n=Nt(6,2);break;case`SSS`:n=Nt(6,3);break;case`Z`:case`ZZ`:case`ZZZ`:n=nu(0);break;case`ZZZZZ`:n=nu(3);break;case`O`:case`OO`:case`OOO`:case`z`:case`zz`:case`zzz`:n=nu(1);break;case`OOOO`:case`ZZZZ`:case`zzzz`:n=nu(2);break;default:return null}return ag[t]=n,n}function Fx(t,n){t=t.replace(/:/g,``);let e=Date.parse(`Jan 01, 1970 00:00:00 `+t)/6e4;return isNaN(e)?n:e}function L1(t,n){return t=new Date(t.getTime()),t.setMinutes(t.getMinutes()+n),t}function V1(t,n,e){let a=t.getTimezoneOffset();return L1(t,-1*(Fx(n,a)-a))}function B1(t){if(Sx(t))return t;if(typeof t==`number`&&!isNaN(t))return new Date(t);if(typeof t==`string`){if(t=t.trim(),/^(\d{4}(-\d{1,2}(-\d{1,2})?)?)$/.test(t)){let[a,o=1,r=1]=t.split(`-`).map(s=>+s);return ou(a,o-1,r)}let e=parseFloat(t);if(!isNaN(t-e))return new Date(e);let i;if(i=t.match(k1))return j1(i)}let n=new Date(t);if(!Sx(n))throw new $(2311,!1);return n}function j1(t){let n=new Date(0),e=0,i=0,a=t[8]?n.setUTCFullYear:n.setFullYear,o=t[8]?n.setUTCHours:n.setHours;t[9]&&(e=Number(t[9]+t[10]),i=Number(t[9]+t[11])),a.call(n,Number(t[1]),Number(t[2])-1,Number(t[3]));let r=Number(t[4]||0)-e,s=Number(t[5]||0)-i,c=Number(t[6]||0),m=Math.floor(parseFloat(`0.`+(t[7]||0))*1e3);return o.call(n,r,s,c,m),n}function Sx(t){return t instanceof Date&&!isNaN(t.valueOf())}var z1=/^(\d+)?\.((\d+)(-(\d+))?)?$/;var wx=22;var ru=`.`;var mc=`0`;var H1=`;`;var U1=`,`;var og=`#`;function $1(t,n,e,i,a,o,r=!1){let s=``,c=!1;if(!isFinite(t))s=Si(e,Number.isNaN(t)?rn.NaN:rn.Infinity);else{let m=q1(t);r&&(m=W1(m));let u=n.minInt,g=n.minFrac,v=n.maxFrac;if(o){let j=o.match(z1);if(j===null)throw new $(2306,!1);let x=j[1],k=j[3],I=j[5];x!=null&&(u=rg(x)),k!=null&&(g=rg(k)),I!=null?v=rg(I):k!=null&&g>v&&(v=g);let U=100;if(u>U||g>U||v>U)throw new $(2306,!1)}Y1(m,g,v);let b=m.digits,y=m.integerLen,E=m.exponent,R=[];for(c=b.every(j=>!j);y<u;y++)b.unshift(0);for(;y<0;y++)b.unshift(0);y>0?R=b.splice(y,b.length):(R=b,b=[0]);let oe=[];for(b.length>=n.lgSize&&oe.unshift(b.splice(-n.lgSize,b.length).join(``));b.length>n.gSize;)oe.unshift(b.splice(-n.gSize,b.length).join(``));b.length&&oe.unshift(b.join(``)),s=oe.join(Si(e,i)),R.length&&(s+=Si(e,a)+R.join(``)),E&&(s+=Si(e,rn.Exponential)+`+`+E)}return t<0&&!c?s=n.negPre+s+n.negSuf:s=n.posPre+s+n.posSuf,s}function Lx(t,n,e){return $1(t,G1(Nx(n,sg.Decimal),Si(n,rn.MinusSign)),n,rn.Group,rn.Decimal,e)}function G1(t,n=`-`){let e={minInt:1,minFrac:0,maxFrac:0,posPre:``,posSuf:``,negPre:``,negSuf:``,gSize:0,lgSize:0},i=t.split(H1),a=i[0],o=i[1],r=a.indexOf(ru)!==-1?a.split(ru):[a.substring(0,a.lastIndexOf(mc)+1),a.substring(a.lastIndexOf(mc)+1)],s=r[0],c=r[1]||``;e.posPre=s.substring(0,s.indexOf(og));for(let u=0;u<c.length;u++){let g=c.charAt(u);g===mc?e.minFrac=e.maxFrac=u+1:g===og?e.maxFrac=u+1:e.posSuf+=g}let m=s.split(U1);if(e.gSize=m[1]?m[1].length:0,e.lgSize=m[2]||m[1]?(m[2]||m[1]).length:0,o){let u=a.length-e.posPre.length-e.posSuf.length,g=o.indexOf(og);e.negPre=o.substring(0,g).replace(/'/g,``),e.negSuf=o.slice(g+u).replace(/'/g,``)}else e.negPre=n+e.posPre,e.negSuf=e.posSuf;return e}function W1(t){if(t.digits[0]===0)return t;let n=t.digits.length-t.integerLen;return t.exponent?t.exponent+=2:(n===0?t.digits.push(0,0):n===1&&t.digits.push(0),t.integerLen+=2),t}function q1(t){let n=Math.abs(t)+``,e=0,i,a,o,r,s;for((a=n.indexOf(ru))>-1&&(n=n.replace(ru,``)),(o=n.search(/e/i))>0?(a<0&&(a=o),a+=+n.slice(o+1),n=n.substring(0,o)):a<0&&(a=n.length),o=0;n.charAt(o)===mc;o++);if(o===(s=n.length))i=[0],a=1;else{for(s--;n.charAt(s)===mc;)s--;for(a-=o,i=[],r=0;o<=s;o++,r++)i[r]=Number(n.charAt(o))}return a>wx&&(i=i.splice(0,wx-1),e=a-1,a=1),{digits:i,exponent:e,integerLen:a}}function Y1(t,n,e){if(n>e)throw new $(2307,!1);let i=t.digits,a=i.length-t.integerLen,o=Math.min(Math.max(n,a),e),r=o+t.integerLen,s=i[r];if(r>0){i.splice(Math.max(t.integerLen,r));for(let g=r;g<i.length;g++)i[g]=0}else{a=Math.max(0,a),t.integerLen=1,i.length=Math.max(1,r=o+1),i[0]=0;for(let g=1;g<r;g++)i[g]=0}if(s>=5)if(r-1<0){for(let g=0;g>r;g--)i.unshift(0),t.integerLen++;i.unshift(1),t.integerLen++}else i[r-1]++;for(;a<Math.max(0,o);a++)i.push(0);let c=o!==0,m=n+t.integerLen,u=i.reduceRight(function(g,v,b,y){return v=v+g,y[b]=v<10?v:v-10,c&&(y[b]===0&&b>=m?y.pop():c=!1),v>=10?1:0},0);u&&(i.unshift(u),t.integerLen++)}function rg(t){let n=parseInt(t);if(isNaN(n))throw new $(2305,!1);return n}var cg=(()=>{class t{_viewContainerRef;ngComponentOutlet=null;ngComponentOutletInputs;ngComponentOutletInjector;ngComponentOutletEnvironmentInjector;ngComponentOutletContent;ngComponentOutletNgModule;_componentRef;_moduleRef;_inputsUsed=new Map;get componentInstance(){return this._componentRef?.instance??null}constructor(e){this._viewContainerRef=e}_needToReCreateNgModuleInstance(e){return e.ngComponentOutletNgModule!==void 0}_needToReCreateComponentInstance(e){return e.ngComponentOutlet!==void 0||e.ngComponentOutletContent!==void 0||e.ngComponentOutletInjector!==void 0||e.ngComponentOutletEnvironmentInjector!==void 0||this._needToReCreateNgModuleInstance(e)}ngOnChanges(e){if(this._needToReCreateComponentInstance(e)&&(this._viewContainerRef.clear(),this._inputsUsed.clear(),this._componentRef=void 0,this.ngComponentOutlet)){let i=this.ngComponentOutletInjector||this._viewContainerRef.parentInjector;this._needToReCreateNgModuleInstance(e)&&(this._moduleRef?.destroy(),this.ngComponentOutletNgModule?this._moduleRef=Vf(this.ngComponentOutletNgModule,K1(i)):this._moduleRef=void 0),this._componentRef=this._viewContainerRef.createComponent(this.ngComponentOutlet,{injector:i,ngModuleRef:this._moduleRef,projectableNodes:this.ngComponentOutletContent,environmentInjector:this.ngComponentOutletEnvironmentInjector})}}ngDoCheck(){if(this._componentRef){if(this.ngComponentOutletInputs)for(let e of Object.keys(this.ngComponentOutletInputs))this._inputsUsed.set(e,!0);this._applyInputStateDiff(this._componentRef)}}ngOnDestroy(){this._moduleRef?.destroy()}_applyInputStateDiff(e){for(let[i,a]of this._inputsUsed)a?(e.setInput(i,this.ngComponentOutletInputs[i]),this._inputsUsed.set(i,!1)):(e.setInput(i,void 0),this._inputsUsed.delete(i))}static ɵfac=function(i){return new(i||t)(je(Lt))};static ɵdir=q({type:t,selectors:[[``,`ngComponentOutlet`,``]],inputs:{ngComponentOutlet:`ngComponentOutlet`,ngComponentOutletInputs:`ngComponentOutletInputs`,ngComponentOutletInjector:`ngComponentOutletInjector`,ngComponentOutletEnvironmentInjector:`ngComponentOutletEnvironmentInjector`,ngComponentOutletContent:`ngComponentOutletContent`,ngComponentOutletNgModule:`ngComponentOutletNgModule`},exportAs:[`ngComponentOutlet`],features:[ft]})}return t})();function K1(t){return t.get(ti).injector}var pc=(()=>{class t{_viewContainerRef;_viewRef=null;ngTemplateOutletContext=null;ngTemplateOutlet=null;ngTemplateOutletInjector=null;injector=f(re);constructor(e){this._viewContainerRef=e}ngOnChanges(e){if(this._shouldRecreateView(e)){let i=this._viewContainerRef;if(this._viewRef&&i.remove(i.indexOf(this._viewRef)),!this.ngTemplateOutlet){this._viewRef=null;return}let a=this._createContextForwardProxy();this._viewRef=i.createEmbeddedView(this.ngTemplateOutlet,a,{injector:this._getInjector()})}}_getInjector(){return this.ngTemplateOutletInjector===`outlet`?this.injector:this.ngTemplateOutletInjector??void 0}_shouldRecreateView(e){return!!e.ngTemplateOutlet||!!e.ngTemplateOutletInjector}_createContextForwardProxy(){return new Proxy({},{set:(e,i,a)=>this.ngTemplateOutletContext?Reflect.set(this.ngTemplateOutletContext,i,a):!1,get:(e,i,a)=>{if(this.ngTemplateOutletContext)return Reflect.get(this.ngTemplateOutletContext,i,a)}})}static ɵfac=function(i){return new(i||t)(je(Lt))};static ɵdir=q({type:t,selectors:[[``,`ngTemplateOutlet`,``]],inputs:{ngTemplateOutletContext:`ngTemplateOutletContext`,ngTemplateOutlet:`ngTemplateOutlet`,ngTemplateOutletInjector:`ngTemplateOutletInjector`},features:[ft]})}return t})();function Vx(t,n){return new $(2100,!1)}var Q1=`mediumDate`;var Bx=new D(``);var jx=new D(``);var Qi=(()=>{class t{locale;defaultTimezone;defaultOptions;constructor(e,i,a){this.locale=e,this.defaultTimezone=i,this.defaultOptions=a}transform(e,i,a,o){if(e==null||e===``||e!==e)return null;try{let r=i??this.defaultOptions?.dateFormat??Q1,s=a??this.defaultOptions?.timezone??this.defaultTimezone??void 0;return Rx(e,r,o||this.locale,s)}catch(r){throw Vx(t,r.message)}}static ɵfac=function(i){return new(i||t)(je(kr,16),je(Bx,24),je(jx,24))};static ɵpipe=ec({name:`date`,type:t,pure:!0})}return t})();var xo=(()=>{class t{transform(e){return JSON.stringify(e,null,2)}static ɵfac=function(i){return new(i||t)};static ɵpipe=ec({name:`json`,type:t,pure:!1})}return t})();var Sn=(()=>{class t{_locale;constructor(e){this._locale=e}transform(e,i,a){if(!X1(e))return null;a||=this._locale;try{return Lx(Z1(e),a,i)}catch(o){throw Vx(t,o.message)}}static ɵfac=function(i){return new(i||t)(je(kr,16))};static ɵpipe=ec({name:`number`,type:t,pure:!0})}return t})();function X1(t){return!(t==null||t===``||t!==t)}function Z1(t){if(typeof t==`string`&&!isNaN(Number(t)-parseFloat(t)))return Number(t);if(typeof t!=`number`)throw new $(2309,!1);return t}function hc(t,n){n=encodeURIComponent(n);for(let e of t.split(`;`)){let i=e.indexOf(`=`),[a,o]=i==-1?[e,``]:[e.slice(0,i),e.slice(i+1)];if(a.trim()!==n)continue;let r=o;try{r=decodeURIComponent(o)}catch{}return r.length>1&&r[0]===`"`&&r[r.length-1]===`"`&&(r=r.slice(1,-1)),r}return null}var lg=`browser`;var nT=`server`;function zx(t){return t===lg}function Hx(t){return t===nT}var fc=class{_doc;constructor(n){this._doc=n}manager};var su=(()=>{class t extends fc{constructor(e){super(e)}supports(e){return!0}addEventListener(e,i,a,o){return e.addEventListener(i,a,o),()=>this.removeEventListener(e,i,a,o)}removeEventListener(e,i,a,o){return e.removeEventListener(i,a,o)}static ɵfac=function(i){return new(i||t)(Se(ce))};static ɵprov=he({token:t,factory:t.ɵfac})}return t})();var du=new D(``);var pg=(()=>{class t{_zone;_plugins;_eventNameToPlugin=new Map;constructor(e,i){this._zone=i,e.forEach(r=>{r.manager=this});let a=e.filter(r=>!(r instanceof su));this._plugins=a.slice().reverse();let o=e.find(r=>r instanceof su);o&&this._plugins.push(o)}addEventListener(e,i,a,o){return this._findPluginFor(i).addEventListener(e,i,a,o)}getZone(){return this._zone}_findPluginFor(e){let i=this._eventNameToPlugin.get(e);if(i)return i;if(i=this._plugins.find(o=>o.supports(e)),!i)throw new $(-5101,!1);return this._eventNameToPlugin.set(e,i),i}static ɵfac=function(i){return new(i||t)(Se(du),Se(K))};static ɵprov=he({token:t,factory:t.ɵfac})}return t})();var dg=`ng-app-id`;function Ux(t){for(let n of t)n.remove()}function $x(t,n){let e=n.createElement(`style`);return e.textContent=t,e}function iT(t,n,e,i){let a=t.head?.querySelectorAll(`style[${dg}="${n}"],link[${dg}="${n}"]`);if(!a||a.length===0)return!1;for(let o of a)o.removeAttribute(dg),o instanceof HTMLLinkElement?i.set(o.href.slice(o.href.lastIndexOf(`/`)+1),{usage:0,elements:[o]}):o.textContent&&e.set(o.textContent,{usage:0,elements:[o]});return!0}function mg(t,n){let e=n.createElement(`link`);return e.setAttribute(`rel`,`stylesheet`),e.setAttribute(`href`,t),e}var hg=(()=>{class t{doc;appId;nonce;inline=new Map;external=new Map;hosts=new Set;constructor(e,i,a,o={}){this.doc=e,this.appId=i,this.nonce=a,iT(e,i,this.inline,this.external)&&this.hosts.add(e.head)}addStyles(e,i){for(let a of e)this.addUsage(a,this.inline,$x);i?.forEach(a=>this.addUsage(a,this.external,mg))}removeStyles(e,i){for(let a of e)this.removeUsage(a,this.inline);i?.forEach(a=>this.removeUsage(a,this.external))}addUsage(e,i,a){let o=i.get(e);o?o.usage++:i.set(e,{usage:1,elements:[...this.hosts].map(r=>this.addElement(r,a(e,this.doc)))})}removeUsage(e,i){let a=i.get(e);a&&(a.usage--,a.usage<=0&&(Ux(a.elements),i.delete(e)))}ngOnDestroy(){for(let[,{elements:e}]of[...this.inline,...this.external])Ux(e);this.hosts.clear()}addHost(e){if(!this.hosts.has(e)){this.hosts.add(e);for(let[i,{elements:a}]of this.inline)a.push(this.addElement(e,$x(i,this.doc)));for(let[i,{elements:a}]of this.external)a.push(this.addElement(e,mg(i,this.doc)))}}removeHost(e){this.hosts.delete(e);for(let i of[...this.inline.values(),...this.external.values()]){let a=[];for(let o of i.elements)o.parentNode===e?o.remove():a.push(o);i.elements=a}}addElement(e,i){return this.nonce&&i.setAttribute(`nonce`,this.nonce),e.appendChild(i)}static ɵfac=function(i){return new(i||t)(Se(ce),Se(Hi),Se(ya,8),Se(Ui))};static ɵprov=he({token:t,factory:t.ɵfac})}return t})();var ug={svg:`http://www.w3.org/2000/svg`,xhtml:`http://www.w3.org/1999/xhtml`,xlink:`http://www.w3.org/1999/xlink`,xml:`http://www.w3.org/XML/1998/namespace`,xmlns:`http://www.w3.org/2000/xmlns/`,math:`http://www.w3.org/1998/Math/MathML`};var fg=/%COMP%/g;var Wx=`%COMP%`;var aT=`_nghost-${Wx}`;var oT=`_ngcontent-${Wx}`;var rT=!0;var sT=new D(``,{factory:()=>rT});var cT=new D(``);function lT(t){return oT.replace(fg,t)}function dT(t){return aT.replace(fg,t)}function qx(t,n){return n.map(e=>e.replace(fg,t))}var gg=(()=>{class t{eventManager;sharedStylesHost;appId;removeStylesOnCompDestroy;doc;ngZone;nonce;tracingService;rendererByCompId=new Map;defaultRenderer;cssVarNamespace;constructor(e,i,a,o,r,s,c=null,m=null,u=null){this.eventManager=e,this.sharedStylesHost=i,this.appId=a,this.removeStylesOnCompDestroy=o,this.doc=r,this.ngZone=s,this.nonce=c,this.tracingService=m,this.cssVarNamespace=u??``,this.defaultRenderer=new gc(e,r,s,this.tracingService,this.cssVarNamespace)}createRenderer(e,i){if(!e||!i)return this.defaultRenderer;let a=this.getOrCreateRenderer(e,i);return a instanceof lu?a.applyToHost(e):a instanceof bc&&a.applyStyles(),a}getOrCreateRenderer(e,i){let a=this.rendererByCompId,o=a.get(i.id);if(!o){let r=this.doc,s=this.ngZone,c=this.eventManager,m=this.sharedStylesHost,u=this.removeStylesOnCompDestroy,g=this.tracingService;switch(i.encapsulation){case Jn.Emulated:o=new lu(c,m,i,this.appId,u,r,s,g,this.cssVarNamespace);break;case Jn.ShadowDom:return new cu(c,e,i,r,s,this.nonce,g,this.cssVarNamespace,m);case Jn.ExperimentalIsolatedShadowDom:return new cu(c,e,i,r,s,this.nonce,g,this.cssVarNamespace);default:o=new bc(c,m,i,u,r,s,g,this.cssVarNamespace)}a.set(i.id,o)}return o}ngOnDestroy(){this.rendererByCompId.clear()}componentReplaced(e){this.rendererByCompId.delete(e)}static ɵfac=function(i){return new(i||t)(Se(pg),Se(yo),Se(Hi),Se(sT),Se(ce),Se(K),Se(ya),Se(ni,8),Se(cT,8))};static ɵprov=he({token:t,factory:t.ɵfac})}return t})();var gc=class{eventManager;doc;ngZone;tracingService;cssVarNamespace;data=Object.create(null);throwOnSyntheticProps=!0;constructor(n,e,i,a,o=``){this.eventManager=n,this.doc=e,this.ngZone=i,this.tracingService=a,this.cssVarNamespace=o}destroy(){}destroyNode=null;createElement(n,e){return e?this.doc.createElementNS(ug[e]||e,n):this.doc.createElement(n)}createComment(n){return this.doc.createComment(n)}createText(n){return this.doc.createTextNode(n)}appendChild(n,e){(Gx(n)?n.content:n).appendChild(e)}insertBefore(n,e,i){if(n){let a=Gx(n)?n.content:n;if(i!=null&&i.parentNode!==a)throw new $(-5106,!1);a.insertBefore(e,i)}}removeChild(n,e){e.remove()}selectRootElement(n,e){let i=typeof n==`string`?this.doc.querySelector(n):n;if(!i)throw new $(-5104,!1);return e||(i.textContent=``),i}parentNode(n){return n.parentNode}nextSibling(n){return n.nextSibling}setAttribute(n,e,i,a){if(a){e=a+`:`+e;let o=ug[a];o?n.setAttributeNS(o,e,i):n.setAttribute(e,i)}else n.setAttribute(e,i)}removeAttribute(n,e,i){if(i){let a=ug[i];a?n.removeAttributeNS(a,e):n.removeAttribute(`${i}:${e}`)}else n.removeAttribute(e)}addClass(n,e){n.classList.add(e)}removeClass(n,e){n.classList.remove(e)}setStyle(n,e,i,a){let o=e.startsWith(`--`);o&&(e=e.replace(`%NS%`,this.cssVarNamespace)),o||a&(xi.DashCase|xi.Important)?n.style.setProperty(e,i,a&xi.Important?`important`:``):n.style[e]=i}removeStyle(n,e,i){let a=e.startsWith(`--`);a&&(e=e.replace(`%NS%`,this.cssVarNamespace)),a||i&xi.DashCase?n.style.removeProperty(e):n.style[e]=``}setProperty(n,e,i){n!=null&&(n[e]=i)}setValue(n,e){n.nodeValue=e}listen(n,e,i,a){if(typeof n==`string`&&(n=Pn().getGlobalEventTarget(this.doc,n),!n))throw new $(-5102,!1);let o=this.decoratePreventDefault(i);return this.tracingService?.wrapEventListener&&(o=this.tracingService.wrapEventListener(n,e,o)),this.eventManager.addEventListener(n,e,o,a)}decoratePreventDefault(n){return e=>{if(e===`__ngUnwrap__`)return n;n(e)===!1&&e.preventDefault()}}};function Gx(t){return t.tagName===`TEMPLATE`&&t.content!==void 0}var cu=class extends gc{hostEl;sharedStylesHost;shadowRoot;constructor(n,e,i,a,o,r,s,c,m){super(n,a,o,s,c),this.hostEl=e,this.sharedStylesHost=m,this.shadowRoot=e.attachShadow({mode:`open`}),this.sharedStylesHost&&this.sharedStylesHost.addHost(this.shadowRoot);let u=i.styles;u=qx(i.id,u).map(v=>v.replace(/%NS%/g,c));for(let v of u){let b=document.createElement(`style`);r&&b.setAttribute(`nonce`,r),b.textContent=v,this.shadowRoot.appendChild(b)}let g=i.getExternalStyles?.();if(g)for(let v of g){let b=mg(v,a);r&&b.setAttribute(`nonce`,r),this.shadowRoot.appendChild(b)}}nodeOrShadowRoot(n){return n===this.hostEl?this.shadowRoot:n}appendChild(n,e){return super.appendChild(this.nodeOrShadowRoot(n),e)}insertBefore(n,e,i){return super.insertBefore(this.nodeOrShadowRoot(n),e,i)}removeChild(n,e){return super.removeChild(null,e)}parentNode(n){return this.nodeOrShadowRoot(super.parentNode(this.nodeOrShadowRoot(n)))}destroy(){this.sharedStylesHost&&this.sharedStylesHost.removeHost(this.shadowRoot)}};var bc=class extends gc{sharedStylesHost;removeStylesOnCompDestroy;styles;styleUrls;constructor(n,e,i,a,o,r,s,c,m){super(n,o,r,s,c),this.sharedStylesHost=e,this.removeStylesOnCompDestroy=a;let u=i.styles,g=m?qx(m,u):u;this.styles=g.map(v=>v.replace(/%NS%/g,c)),this.styleUrls=i.getExternalStyles?.(m)}applyStyles(){this.sharedStylesHost.addStyles(this.styles,this.styleUrls)}destroy(){this.removeStylesOnCompDestroy&&hf.size===0&&this.sharedStylesHost.removeStyles(this.styles,this.styleUrls)}};var lu=class extends bc{contentAttr;hostAttr;constructor(n,e,i,a,o,r,s,c,m){let u=a+`-`+i.id;super(n,e,i,o,r,s,c,m,u),this.contentAttr=lT(u),this.hostAttr=dT(u)}applyToHost(n){this.applyStyles(),this.setAttribute(n,this.hostAttr,``)}createElement(n,e){let i=super.createElement(n,e);return super.setAttribute(i,this.contentAttr,``),i}};var uu=class t extends cc{supportsDOMEvents=!0;static makeCurrent(){tg(new t)}onAndCancel(n,e,i,a){return n.addEventListener(e,i,a),()=>{n.removeEventListener(e,i,a)}}dispatchEvent(n,e){n.dispatchEvent(e)}remove(n){n.remove()}createElement(n,e){return e=e||this.getDefaultDocument(),e.createElement(n)}createHtmlDocument(){return document.implementation.createHTMLDocument(`fakeTitle`)}getDefaultDocument(){return document}isElementNode(n){return n.nodeType===Node.ELEMENT_NODE}isShadowRoot(n){return n instanceof DocumentFragment}getGlobalEventTarget(n,e){return e===`window`?window:e===`document`?n:e===`body`?n.body:null}getBaseHref(n){let e=uT();return e==null?null:mT(e)}resetBaseElement(){vc=null}getUserAgent(){return window.navigator.userAgent}getCookie(n){return hc(document.cookie,n)}};var vc=null;function uT(){return vc=vc||document.head.querySelector(`base`),vc?vc.getAttribute(`href`):null}function mT(t){return new URL(t,document.baseURI).pathname}var Yx=[`alt`,`control`,`meta`,`shift`];var pT={"\b":`Backspace`,"	":`Tab`,"":`Delete`,"\x1B":`Escape`,Del:`Delete`,Esc:`Escape`,Left:`ArrowLeft`,Right:`ArrowRight`,Up:`ArrowUp`,Down:`ArrowDown`,Menu:`ContextMenu`,Scroll:`ScrollLock`,Win:`OS`};var hT={alt:t=>t.altKey,control:t=>t.ctrlKey,meta:t=>t.metaKey,shift:t=>t.shiftKey};var Kx=(()=>{class t extends fc{constructor(e){super(e)}supports(e){return t.parseEventName(e)!=null}addEventListener(e,i,a,o){let r=t.parseEventName(i),s=t.eventCallback(r.fullKey,a,this.manager.getZone());return this.manager.getZone().runOutsideAngular(()=>Pn().onAndCancel(e,r.domEventName,s,o))}static parseEventName(e){let i=e.toLowerCase().split(`.`),a=i.shift();if(i.length===0||!(a===`keydown`||a===`keyup`))return null;let o=t._normalizeKey(i.pop()),r=``,s=i.indexOf(`code`);if(s>-1&&(i.splice(s,1),r=`code.`),Yx.forEach(m=>{let u=i.indexOf(m);u>-1&&(i.splice(u,1),r+=m+`.`)}),r+=o,i.length!=0||o.length===0)return null;let c={};return c.domEventName=a,c.fullKey=r,c}static matchEventFullKeyCode(e,i){let a=pT[e.key]||e.key,o=``;return i.indexOf(`code.`)>-1&&(a=e.code,o=`code.`),a==null||!a?!1:(a=a.toLowerCase(),a===` `?a=`space`:a===`.`&&(a=`dot`),Yx.forEach(r=>{if(r!==a){let s=hT[r];s(e)&&(o+=r+`.`)}}),o+=a,o===i)}static eventCallback(e,i,a){return o=>{t.matchEventFullKeyCode(o,e)&&a.runGuarded(()=>i(o))}}static _normalizeKey(e){return e===`esc`?`escape`:e}static ɵfac=function(i){return new(i||t)(Se(ce))};static ɵprov=he({token:t,factory:t.ɵfac})}return t})();async function bg(t,n,e){return gx(r({rootComponent:t},fT(n,e)))}function fT(t,n){return{platformRef:n?.platformRef,appProviders:[..._T,...t?.providers??[]],platformProviders:yT}}function gT(){uu.makeCurrent()}function bT(){return new Mn}function vT(){return af(document),document}var yT=[{provide:Ui,useValue:lg},{provide:dd,useValue:gT,multi:!0},{provide:ce,useFactory:vT}];var _T=[{provide:ks,useValue:`root`},{provide:Mn,useFactory:bT},{provide:du,useClass:su,multi:!0},{provide:du,useClass:Kx,multi:!0},gg,{provide:yo,useClass:hg},{provide:hg,useExisting:yo},pg,{provide:kt,useExisting:gg},[]];var Zi=class t{headers;normalizedNames=new Map;lazyInit;lazyUpdate=null;constructor(n){n?typeof n==`string`?this.lazyInit=()=>{this.headers=new Map,n.split(`
`).forEach(e=>{let i=e.indexOf(`:`);if(i>0){let a=e.slice(0,i),o=e.slice(i+1).trim();this.addHeaderEntry(a,o)}})}:typeof Headers<`u`&&n instanceof Headers?(this.headers=new Map,n.forEach((e,i)=>{this.addHeaderEntry(i,e)})):this.lazyInit=()=>{this.headers=new Map,Object.entries(n).forEach(([e,i])=>{this.setHeaderEntries(e,i)})}:this.headers=new Map}has(n){return this.init(),this.headers.has(n.toLowerCase())}get(n){this.init();let e=this.headers.get(n.toLowerCase());return e&&e.length>0?e[0]:null}keys(){return this.init(),Array.from(this.normalizedNames.values())}getAll(n){return this.init(),this.headers.get(n.toLowerCase())||null}append(n,e){return this.clone({name:n,value:e,op:`a`})}set(n,e){return this.clone({name:n,value:e,op:`s`})}delete(n,e){return this.clone({name:n,value:e,op:`d`})}maybeSetNormalizedName(n,e){this.normalizedNames.has(e)||this.normalizedNames.set(e,n)}init(){this.lazyInit&&(this.lazyInit instanceof t?this.copyFrom(this.lazyInit):this.lazyInit(),this.lazyInit=null,this.lazyUpdate&&(this.lazyUpdate.forEach(n=>this.applyUpdate(n)),this.lazyUpdate=null))}copyFrom(n){n.init();for(let[e,i]of n.headers.entries())this.headers.set(e,i),this.normalizedNames.set(e,n.normalizedNames.get(e))}clone(n){let e=new t;return e.lazyInit=this.lazyInit&&this.lazyInit instanceof t?this.lazyInit:this,e.lazyUpdate=(this.lazyUpdate||[]).concat([n]),e}applyUpdate(n){let e=n.name.toLowerCase();switch(n.op){case`a`:case`s`:let i=n.value;if(typeof i==`string`&&(i=[i]),i.length===0)return;this.maybeSetNormalizedName(n.name,e);let a=n.op===`a`?(this.headers.get(e)||[]).slice():[];a.push(...i),this.headers.set(e,a);break;case`d`:let o=n.value;if(o===void 0)this.headers.delete(e),this.normalizedNames.delete(e);else{let r=Array.isArray(o)?o:[o],s=this.headers.get(e);if(!s)return;s=s.filter(c=>r.indexOf(c)===-1),s.length===0?(this.headers.delete(e),this.normalizedNames.delete(e)):this.headers.set(e,s)}}}addHeaderEntry(n,e){let i=n.toLowerCase();this.maybeSetNormalizedName(n,i),this.headers.has(i)?this.headers.get(i).push(e):this.headers.set(i,[e])}setHeaderEntries(n,e){let i=(Array.isArray(e)?e:[e]).map(o=>o.toString()),a=n.toLowerCase();this.headers.set(a,i),this.maybeSetNormalizedName(n,a)}forEach(n){this.init(),Array.from(this.normalizedNames.keys()).forEach(e=>n(this.normalizedNames.get(e),this.headers.get(e)))}};var pu=class{map=new Map;set(n,e){return this.map.set(n,e),this}get(n){return this.map.has(n)||this.map.set(n,n.defaultValue()),this.map.get(n)}delete(n){return this.map.delete(n),this}has(n){return this.map.has(n)}keys(){return this.map.keys()}};var hu=class{encodeKey(n){return Qx(n)}encodeValue(n){return Qx(n)}decodeKey(n){return decodeURIComponent(n)}decodeValue(n){return decodeURIComponent(n)}};function xT(t,n){let e=new Map;return t.length>0&&t.replace(/^\?/,``).split(`&`).forEach(a=>{let o=a.indexOf(`=`),[r,s]=o==-1?[n.decodeKey(a),``]:[n.decodeKey(a.slice(0,o)),n.decodeValue(a.slice(o+1))],c=e.get(r)||[];c.push(s),e.set(r,c)}),e}var CT=/%(\d[a-f0-9])/gi;var ST={40:`@`,"3A":`:`,24:`$`,"2C":`,`,"3B":`;`,"3D":`=`,"3F":`?`,"2F":`/`};function Qx(t){return encodeURIComponent(t).replace(CT,(n,e)=>ST[e]??n)}function mu(t){return`${t}`}var Xi=class t{map;encoder;updates=null;cloneFrom=null;constructor(n={}){if(this.encoder=n.encoder||new hu,n.fromString){if(n.fromObject)throw new $(2805,!1);this.map=xT(n.fromString,this.encoder)}else n.fromObject?(this.map=new Map,Object.keys(n.fromObject).forEach(e=>{let i=n.fromObject[e],a=Array.isArray(i)?i.map(mu):[mu(i)];this.map.set(e,a)})):this.map=null}has(n){return this.init(),this.map.has(n)}get(n){this.init();let e=this.map.get(n);return e?e[0]:null}getAll(n){return this.init(),this.map.get(n)||null}keys(){return this.init(),Array.from(this.map.keys())}append(n,e){return this.clone({param:n,value:e,op:`a`})}appendAll(n){let e=[];return Object.keys(n).forEach(i=>{let a=n[i];Array.isArray(a)?a.forEach(o=>{e.push({param:i,value:o,op:`a`})}):e.push({param:i,value:a,op:`a`})}),this.clone(e)}set(n,e){return this.clone({param:n,value:e,op:`s`})}delete(n,e){return this.clone({param:n,value:e,op:`d`})}toString(){return this.init(),this.keys().map(n=>{let e=this.encoder.encodeKey(n);return this.map.get(n).map(i=>e+`=`+this.encoder.encodeValue(i)).join(`&`)}).filter(n=>n!==``).join(`&`)}clone(n){let e=new t({encoder:this.encoder});return e.cloneFrom=this.cloneFrom||this,e.updates=(this.updates||[]).concat(n),e}init(){if(this.map===null&&(this.map=new Map),this.cloneFrom!==null){this.cloneFrom.init();for(let[n,e]of this.cloneFrom.map.entries())this.map.set(n,e);this.updates.forEach(n=>{switch(n.op){case`a`:case`s`:let e=n.op===`a`?(this.map.get(n.param)||[]).slice():[];e.push(mu(n.value)),this.map.set(n.param,e);break;case`d`:if(n.value!==void 0){let i=(this.map.get(n.param)||[]).slice(),a=i.indexOf(mu(n.value));a!==-1&&i.splice(a,1),i.length>0?this.map.set(n.param,i):this.map.delete(n.param)}else{this.map.delete(n.param);break}}}),this.cloneFrom=this.updates=null}}};function wT(t){switch(t){case`DELETE`:case`GET`:case`HEAD`:case`OPTIONS`:case`JSONP`:return!1;default:return!0}}function Xx(t){return typeof ArrayBuffer<`u`&&t instanceof ArrayBuffer}function Zx(t){return typeof Blob<`u`&&t instanceof Blob}function Jx(t){return typeof FormData<`u`&&t instanceof FormData}function ET(t){return typeof URLSearchParams<`u`&&t instanceof URLSearchParams}var vg=`Content-Type`;var eC=`Accept`;var iC=`text/plain`;var aC=`application/json`;var DT=`${aC}, ${iC}, */*`;var Mr=class t{url;body=null;headers;context;reportProgress=!1;reportUploadProgress=!1;reportDownloadProgress=!1;withCredentials=!1;credentials;keepalive=!1;cache;priority;mode;redirect;referrer;integrity;referrerPolicy;responseType=`json`;method;params;urlWithParams;transferCache;timeout;constructor(n,e,i,a){this.url=e,this.method=n.toUpperCase();let o;if(wT(this.method)||a?(this.body=i!==void 0?i:null,o=a):o=i,o){if(this.reportProgress=!!o.reportProgress,this.reportUploadProgress=!!o.reportUploadProgress,this.reportDownloadProgress=!!o.reportDownloadProgress,this.withCredentials=!!o.withCredentials,this.keepalive=!!o.keepalive,o.responseType&&(this.responseType=o.responseType),o.headers&&(this.headers=o.headers),o.context&&(this.context=o.context),o.params&&(this.params=o.params),o.priority&&(this.priority=o.priority),o.cache&&(this.cache=o.cache),o.credentials&&(this.credentials=o.credentials),typeof o.timeout==`number`){if(o.timeout<1||!Number.isInteger(o.timeout))throw new $(2822,``);this.timeout=o.timeout}o.mode&&(this.mode=o.mode),o.redirect&&(this.redirect=o.redirect),o.integrity&&(this.integrity=o.integrity),o.referrer!==void 0&&(this.referrer=o.referrer),o.referrerPolicy&&(this.referrerPolicy=o.referrerPolicy),this.transferCache=o.transferCache}if(this.headers??=new Zi,this.context??=new pu,!this.params)this.params=new Xi,this.urlWithParams=e;else{let r=this.params.toString();if(r.length===0)this.urlWithParams=e;else{let s=e,c=``,m=e.indexOf(`#`);m!==-1&&(c=e.substring(m),s=e.substring(0,m));let u=s.indexOf(`?`),g=u===-1?`?`:u<s.length-1?`&`:``;this.urlWithParams=s+g+r+c}}}serializeBody(){return this.body===null?null:typeof this.body==`string`||Xx(this.body)||Zx(this.body)||Jx(this.body)||ET(this.body)?this.body:this.body instanceof Xi?this.body.toString():typeof this.body==`object`||typeof this.body==`boolean`||Array.isArray(this.body)?JSON.stringify(this.body):this.body.toString()}detectContentTypeHeader(){return this.body===null||Jx(this.body)?null:Zx(this.body)?this.body.type||null:Xx(this.body)?null:typeof this.body==`string`?iC:this.body instanceof Xi?`application/x-www-form-urlencoded;charset=UTF-8`:typeof this.body==`object`||typeof this.body==`number`||typeof this.body==`boolean`?aC:null}clone(n={}){let e=n.method||this.method,i=n.url||this.url,a=n.responseType||this.responseType,o=n.keepalive??this.keepalive,r=n.priority||this.priority,s=n.cache||this.cache,c=n.mode||this.mode,m=n.redirect||this.redirect,u=n.credentials||this.credentials,g=n.referrer??this.referrer,v=n.integrity||this.integrity,b=n.referrerPolicy||this.referrerPolicy,y=n.transferCache??this.transferCache,E=n.timeout??this.timeout,R=n.body!==void 0?n.body:this.body,oe=n.withCredentials??this.withCredentials,j=n.reportProgress??this.reportProgress,x=n.reportUploadProgress??this.reportUploadProgress,k=n.reportDownloadProgress??this.reportDownloadProgress,I=n.headers||this.headers,U=n.params||this.params,ge=n.context??this.context;return n.setHeaders!==void 0&&(I=Object.keys(n.setHeaders).reduce((ht,Ge)=>ht.set(Ge,n.setHeaders[Ge]),I)),n.setParams&&(U=Object.keys(n.setParams).reduce((ht,Ge)=>ht.set(Ge,n.setParams[Ge]),U)),new t(e,i,R,{params:U,headers:I,context:ge,reportProgress:j,reportUploadProgress:x,reportDownloadProgress:k,responseType:a,withCredentials:oe,transferCache:y,keepalive:o,cache:s,priority:r,timeout:E,mode:c,redirect:m,credentials:u,referrer:g,integrity:v,referrerPolicy:b})}};var Co=(function(t){return t[t.Sent=0]=`Sent`,t[t.UploadProgress=1]=`UploadProgress`,t[t.ResponseHeader=2]=`ResponseHeader`,t[t.DownloadProgress=3]=`DownloadProgress`,t[t.Response=4]=`Response`,t[t.User=5]=`User`,t})(Co||{});var Nr=class{headers;status;statusText;url;ok;type;redirected;responseType;constructor(n,e=200,i=`OK`){this.headers=n.headers||new Zi,this.status=n.status!==void 0?n.status:e,this.statusText=n.statusText||i,this.url=n.url||null,this.redirected=n.redirected,this.responseType=n.responseType,this.ok=this.status>=200&&this.status<300}};var fu=class t extends Nr{constructor(n={}){super(n)}type=Co.ResponseHeader;clone(n={}){return new t({headers:n.headers||this.headers,status:n.status!==void 0?n.status:this.status,statusText:n.statusText||this.statusText,url:n.url||this.url||void 0})}};var yc=class t extends Nr{body;constructor(n={}){super(n),this.body=n.body!==void 0?n.body:null}type=Co.Response;clone(n={}){return new t({body:n.body!==void 0?n.body:this.body,headers:n.headers||this.headers,status:n.status!==void 0?n.status:this.status,statusText:n.statusText||this.statusText,url:n.url||this.url||void 0,redirected:n.redirected??this.redirected,responseType:n.responseType??this.responseType})}};var wi=class extends Nr{name=`HttpErrorResponse`;message;error;ok=!1;constructor(n){super(n,0,`Unknown Error`),this.status>=200&&this.status<300?this.message=`Http failure during parsing for ${n.url||`(unknown url)`}`:this.message=`Http failure response for ${n.url||`(unknown url)`}: ${n.status} ${n.statusText}`,this.error=n.error||null}};var kT=200;var IT=/^\)\]\}',?\n/;var oC=new D(``,{factory:()=>null});var gu=(()=>{class t{fetchImpl=f(_g,{optional:!0})?.fetch??((...e)=>globalThis.fetch(...e));ngZone=f(K);destroyRef=f(Ne);maxResponseSize=f(oC);handle(e){return new xe(i=>{let a=new AbortController,o=!1,r={next:c=>{c.type===Co.Response&&(o=!0),i.next(c)},error:c=>{o=!0,i.error(c)},complete:()=>{o=!0,i.complete()}};this.doRequest(e,a.signal,r).then(xg,c=>r.error(new wi({error:c})));let s;return e.timeout&&(s=this.ngZone.runOutsideAngular(()=>setTimeout(()=>{a.signal.aborted||a.abort(new DOMException(`signal timed out`,`TimeoutError`))},e.timeout))),()=>{s!==void 0&&clearTimeout(s),!o&&!a.signal.aborted&&a.abort()}})}async doRequest(e,i,a){let o=this.createRequestInit(e),r$1;try{let R=this.ngZone.runOutsideAngular(()=>this.fetchImpl(e.urlWithParams,r({signal:i},o)));MT(R),a.next({type:Co.Sent}),r$1=await R}catch(R){a.error(new wi({error:R,status:R.status??0,statusText:R.statusText,url:e.urlWithParams,headers:R.headers}));return}let s=new Zi(r$1.headers),c=r$1.statusText,m=r$1.url||e.urlWithParams,u=r$1.status,g=null,v=e.reportProgress||e.reportDownloadProgress;if(v&&a.next(new fu({headers:s,status:u,statusText:c,url:m})),r$1.body){let R=r$1.headers.get(vg)??``,oe=r$1.headers.get(`content-length`),j=oe!==null?Number(oe):NaN;this.maxResponseSize!==null&&Number.isFinite(j)&&j>this.maxResponseSize&&(await r$1.body.cancel(),tC(this.maxResponseSize));let x=[],k=r$1.body.getReader(),I=0,U,ge,ht=typeof Zone<`u`&&Zone.current,Ge=!1;if(await this.ngZone.runOutsideAngular(async()=>{for(;;){if(this.destroyRef.destroyed){await k.cancel(),Ge=!0;break}let{done:ke,value:F}=await k.read();if(ke)break;if(x.push(F),I+=F.length,this.maxResponseSize!==null&&I>this.maxResponseSize&&(await k.cancel(),tC(this.maxResponseSize)),v){ge=e.responseType===`text`?(ge??``)+(U??=nC(R)).decode(F,{stream:!0}):void 0;let We=()=>a.next({type:Co.DownloadProgress,total:Number.isFinite(j)?j:void 0,loaded:I,partialText:ge});ht?ht.run(We):We()}}}),Ge){a.complete();return}let le=this.concatChunks(x,I);try{g=this.parseBody(e,le,R,u)}catch(ke){a.error(new wi({error:ke,headers:new Zi(r$1.headers),status:r$1.status,statusText:r$1.statusText,url:r$1.url||e.urlWithParams}));return}}u===0&&(u=g?kT:0);let b=u>=200&&u<300,y=r$1.redirected,E=r$1.type;b?(a.next(new yc({body:g,headers:s,status:u,statusText:c,url:m,redirected:y,responseType:E})),a.complete()):a.error(new wi({error:g,headers:s,status:u,statusText:c,url:m,redirected:y,responseType:E}))}parseBody(e,i,a,o){switch(e.responseType){case`json`:let r=new TextDecoder().decode(i).replace(IT,``);if(r===``)return null;try{return JSON.parse(r)}catch(s){if(o<200||o>=300)return r;throw s}case`text`:return nC(a).decode(i);case`blob`:return new Blob([i],{type:a});case`arraybuffer`:return i.buffer}}createRequestInit(e){if(e.reportUploadProgress)throw new $(2824,!1);let i={},a;if(a=e.credentials,e.withCredentials&&(a=`include`),e.headers.forEach((o,r)=>i[o]=r.join(`,`)),e.headers.has(eC)||(i[eC]=DT),!e.headers.has(vg)){let o=e.detectContentTypeHeader();o!==null&&(i[vg]=o)}return{body:e.serializeBody(),method:e.method,headers:i,credentials:a,keepalive:e.keepalive,cache:e.cache,priority:e.priority,mode:e.mode,redirect:e.redirect,referrer:e.referrer,integrity:e.integrity,referrerPolicy:e.referrerPolicy}}concatChunks(e,i){let a=new Uint8Array(i),o=0;for(let r of e)a.set(r,o),o+=r.length;return a}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var _g=class{};function xg(){}function MT(t){t.then(xg,xg)}function tC(t){throw new $(-2825,!1)}var NT=/charset=\s*["']?([^;"'\s]+)["']?/i;function nC(t){let n=t.match(NT);if(n!==null)try{return new TextDecoder(n[1])}catch{}return new TextDecoder}var TT=new D(``,{factory:()=>!0});var AT=`XSRF-TOKEN`;var OT=new D(``,{factory:()=>AT});var RT=`X-XSRF-TOKEN`;var PT=new D(``,{factory:()=>RT});var FT=(()=>{class t{cookieName=f(OT);doc=f(ce);lastCookieString=``;lastToken=null;parseCount=0;getToken(){let e=this.doc.cookie||``;return e!==this.lastCookieString&&(this.parseCount++,this.lastToken=hc(e,this.cookieName),this.lastCookieString=e),this.lastToken}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var rC=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵprov=he({token:t,factory:function(i){let a=null;return i?a=new(i||t):a=Se(FT),a},providedIn:`root`})}return t})();function sC(t,n){if(!f(TT)||t.method===`GET`||t.method===`HEAD`)return n(t);try{let a=f(Ir).href,{origin:o}=new URL(a),{origin:r}=new URL(t.url,o);if(o!==r)return n(t)}catch{return n(t)}let e=f(rC).getToken(),i=f(PT);return e!=null&&!t.headers.has(i)&&(t=t.clone({headers:t.headers.set(i,e)})),n(t)}function LT(t,n){return n(t)}function VT(t,n,e){return(i,a)=>nr(e,()=>n(i,o=>t(o,a)))}var cC=new D(``,{factory:()=>[sC]});var lC=new D(``);var dC=new D(``,{factory:()=>!0});var Cg=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵprov=he({token:t,factory:function(i){let a=null;return i?a=new(i||t):a=Se(gu),a},providedIn:`root`})}return t})();var bu=(()=>{class t{backend;injector;chain=null;pendingTasks=f(_a);contributeToStability=f(dC);constructor(e,i){this.backend=e,this.injector=i}handle(e){if(this.chain===null){let a=this.injector.get(vu,null,{skipSelf:!0}),o=a!==null&&this.backend===a,r=this.injector.get(lC,[],o?{self:!0}:void 0),s=Array.from(new Set([...this.injector.get(cC),...r]));this.chain=s.reduceRight((c,m)=>VT(c,m,this.injector),LT)}let i=this.chain;if(this.contributeToStability){let a=this.pendingTasks.add();return tt(()=>i(e,o=>this.backend.handle(o))).pipe(ip(a))}else return tt(()=>i(e,a=>this.backend.handle(a)))}static ɵfac=function(i){return new(i||t)(Se(Cg),Se(Et))};static ɵprov=he({token:t,factory:t.ɵfac,providedIn:`root`})}return t})();var vu=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵprov=he({token:t,factory:function(i){let a=null;return i?a=new(i||t):a=Se(bu),a},providedIn:`root`})}return t})();function yg(t,n){return r({body:n},t)}var Ei=(()=>{class t{handler;constructor(e){this.handler=e}request(e,i,a={}){let o;if(e instanceof Mr)o=e;else{let c;a.headers instanceof Zi?c=a.headers:c=new Zi(a.headers);let m;a.params&&(a.params instanceof Xi?m=a.params:m=new Xi({fromObject:a.params})),o=new Mr(e,i,a.body!==void 0?a.body:null,{headers:c,context:a.context,params:m,reportProgress:a.reportProgress,reportUploadProgress:a.reportUploadProgress,reportDownloadProgress:a.reportDownloadProgress,responseType:a.responseType||`json`,withCredentials:a.withCredentials,transferCache:a.transferCache,keepalive:a.keepalive,priority:a.priority,cache:a.cache,mode:a.mode,redirect:a.redirect,credentials:a.credentials,referrer:a.referrer,referrerPolicy:a.referrerPolicy,integrity:a.integrity,timeout:a.timeout})}let r=In(o).pipe(np(c=>this.handler.handle(c)));if(e instanceof Mr||a.observe===`events`)return r;let s=r.pipe(Ke(c=>c instanceof yc));switch(a.observe||`body`){case`body`:switch(o.responseType){case`arraybuffer`:return s.pipe(ot(c=>{if(c.body!==null&&!(c.body instanceof ArrayBuffer))throw new $(2806,!1);return c.body}));case`blob`:return s.pipe(ot(c=>{if(c.body!==null&&!(c.body instanceof Blob))throw new $(2807,!1);return c.body}));case`text`:return s.pipe(ot(c=>{if(c.body!==null&&typeof c.body!=`string`)throw new $(2808,!1);return c.body}));default:return s.pipe(ot(c=>c.body))}case`response`:return s;default:throw new $(2809,!1)}}delete(e,i={}){return this.request(`DELETE`,e,i)}get(e,i={}){return this.request(`GET`,e,i)}head(e,i={}){return this.request(`HEAD`,e,i)}jsonp(e,i){return this.request(`JSONP`,e,{params:new Xi().append(i,`JSONP_CALLBACK`),observe:`body`,responseType:`json`})}options(e,i={}){return this.request(`OPTIONS`,e,i)}patch(e,i,a={}){return this.request(`PATCH`,e,yg(a,i))}post(e,i,a={}){return this.request(`POST`,e,yg(a,i))}put(e,i,a={}){return this.request(`PUT`,e,yg(a,i))}static ɵfac=function(i){return new(i||t)(Se(vu))};static ɵprov=he({token:t,factory:t.ɵfac,providedIn:`root`})}return t})();function Sg(...t){let n=[Ei,gu,bu,{provide:vu,useExisting:bu},{provide:Cg,useFactory:()=>f(gu)},{provide:cC,useValue:sC,multi:!0}];for(let e of t)n.push(...e.ɵproviders);return an(n)}var wg=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵprov=he({token:t,factory:function(i){let a=null;return i?a=new(i||t):a=Se(BT),a},providedIn:`root`})}return t})();var BT=(()=>{class t extends wg{_doc=f(ce);sanitize(e,i){if(i==null)return null;switch(e){case bn.NONE:return i;case bn.HTML:return Ca(i,`HTML`)?ei(i):uf(this._doc,String(i)).toString();case bn.STYLE:return Ca(i,`Style`)?ei(i):i;case bn.SCRIPT:if(Ca(i,`Script`))return ei(i);throw new $(5200,!1);case bn.URL:return Ca(i,`URL`)?ei(i):Ys(String(i));case bn.RESOURCE_URL:if(Ca(i,`ResourceURL`))return ei(i);throw new $(-5201,!1);default:throw new $(5202,!1)}}bypassSecurityTrustHtml(e){return rf(e)}bypassSecurityTrustStyle(e){return sf(e)}bypassSecurityTrustScript(e){return cf(e)}bypassSecurityTrustUrl(e){return lf(e)}bypassSecurityTrustResourceUrl(e){return df(e)}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var uC=new D(`HIGHCHARTS_LOADER`);var mC=new D(`HIGHCHARTS_ROOT_MODULES`);var pC=new D(`HIGHCHARTS_OPTIONS`);var jT=new D(`HIGHCHARTS_CONFIG`);var hC=new D(`HIGHCHARTS_TIMEOUT`);var zT=(()=>{class t{constructor(){this.highcharts=M(null),this.loader=f(uC),this.globalOptions=f(pC,{optional:!0}),this.globalModules=f(mC,{optional:!0}),this.sharedHighchartsPromise=null,this.moduleLoadCache=new WeakMap}async loadModules(e){if(!e)return;let i=this.moduleLoadCache.get(e);if(i)return i;let a=Promise.allSettled(e()).then(o=>{let r=o.filter(s=>s.status===`rejected`);if(r.length){let s=r.map(({reason:c})=>c instanceof Error?c.message:String(c));throw new Error(`Failed to load Highcharts modules: ${s.join(`; `)}`)}});return this.moduleLoadCache.set(e,a),a.catch(()=>{this.moduleLoadCache.get(e)===a&&this.moduleLoadCache.delete(e)}),a}async ensureSharedHighcharts(){if(!this.sharedHighchartsPromise){let e=(async()=>{let i=await this.loader();return await this.loadModules(this.globalModules),this.globalOptions&&i.setOptions(this.globalOptions),i})();this.sharedHighchartsPromise=e,e.catch(()=>{this.sharedHighchartsPromise===e&&(this.sharedHighchartsPromise=null)})}return this.sharedHighchartsPromise}async load(e){let i=await this.ensureSharedHighcharts();return await this.loadModules(e?.modules),this.highcharts.set(i),i}static{this.ɵfac=function(i){return new(i||t)}}static{this.ɵprov=he({token:t,factory:t.ɵfac,providedIn:`root`})}}return t})();var HT=(()=>{class t{delay(e){return new Promise(i=>setTimeout(i,e))}getChartFactory(e,i){if(i===`chart`)return e.chart;let o=e[i];if(!o)throw new Error(`Highcharts constructor "${i}" is not available. Did you load the required module?`);return o}createChart(){Pt(e=>{let i=this.loadedHighcharts(),a=this.constructorType();if(!i||this.isDestroyed)return;let o=s=>{if(!(s.renderer.forExport||this.isDestroyed))return this.chartInstance.emit(s)},r=this.getChartFactory(i,a)(this.el.nativeElement,tt(()=>this.options()),o);this.chart.set(r),e(()=>{this.chart()===r&&this.chart.set(null),r.destroy()})})}keepChartUpToDate(){let e=null;Pt(()=>{let i=this.chart(),a=this.update(),o=this.oneToOne(),r=this.options();if(i){if(i!==e){e=i;return}a&&i.update(r,!0,o)}})}async initializeHighcharts(){await this.pendingTasks.run(async()=>{try{let e=await this.highchartsChartService.load(this.relativeConfig),i=this.relativeConfig?.timeout??this.timeout??0;i>0&&await this.delay(i),this.isDestroyed||this.loadedHighcharts.set(e)}catch(e){console.error(`Highcharts failed to load; chart was not created.`,e)}})}constructor(){this.constructorType=jt(`chart`),this.oneToOne=jt(!1),this.options=jt.required(),this.update=hx(!0),this.chartInstance=Qd(),this.destroyRef=f(Ne),this.el=f(G),this.platformId=f(Ui),this.relativeConfig=f(jT,{optional:!0}),this.timeout=f(hC,{optional:!0}),this.highchartsChartService=f(zT),this.pendingTasks=f(_a),this.loadedHighcharts=M(null),this.chart=M(null),this.isDestroyed=!1,!(this.platformId&&Hx(this.platformId))&&(this.destroyRef.onDestroy(()=>{this.isDestroyed=!0}),this.createChart(),this.keepChartUpToDate(),this.initializeHighcharts())}static{this.ɵfac=function(i){return new(i||t)}}static{this.ɵdir=q({type:t,selectors:[[``,`highchartsChart`,``]],inputs:{constructorType:[1,`constructorType`],oneToOne:[1,`oneToOne`],options:[1,`options`],update:[1,`update`]},outputs:{update:`updateChange`,chartInstance:`chartInstance`}})}}return t})();var fC=(()=>{class t{static{this.ɵfac=function(i){return new(i||t)}}static{this.ɵcmp=B({type:t,selectors:[[`highcharts-chart`]],features:[tc([{directive:HT,inputs:[`constructorType`,`constructorType`,`oneToOne`,`oneToOne`,`options`,`options`,`update`,`update`],outputs:[`chartInstance`,`chartInstance`,`updateChange`,`updateChange`]}])],decls:0,vars:0,template:function(i,a){},encapsulation:2})}}return t})();var UT=()=>[];var $T=()=>import(`./chunk-BLbMvaz5.js`).then(t=>t.default);function GT(t){return an([{provide:uC,useValue:t??$T}])}function WT(t){return an([{provide:pC,useValue:t}])}function qT(t){return an([{provide:mC,useValue:t}])}function gC(t={}){let n=[GT(t.instance),qT(t.modules??UT),{provide:hC,useValue:t.timeout}];return t.options&&n.push(WT(t.options)),an(n)}var bC={providers:[ih(),Kf(),Sg(),gC({instance:()=>import(`./chunk-BLbMvaz5.js`).then(t=>t.default),modules:()=>[Promise.all([import(`./chunk-DCvj9IQN.js`).then(()=>import(`./chunk-Sh_AB3RF.js`)),import(`./chunk-rsitv60z.js`),import(`./chunk-WuwBIxKK.js`),import(`./chunk-DAS5iyZJ.js`),import(`./chunk-8a3oyBAR.js`)]).then(()=>import(`./chunk-CRvOqubi.js`))]})]};function So(t){return t.buttons===0||t.detail===0}function wo(t){let n=t.touches&&t.touches[0]||t.changedTouches&&t.changedTouches[0];return!!n&&n.identifier===-1&&(n.radiusX==null||n.radiusX===1)&&(n.radiusY==null||n.radiusY===1)}var Eg;function vC(){if(Eg==null){let t=typeof document<`u`?document.head:null;Eg=!!(t&&(t.createShadowRoot||t.attachShadow))}return Eg}function Dg(t){if(vC()){let n=t.getRootNode?t.getRootNode():null;if(typeof ShadowRoot<`u`&&ShadowRoot&&n instanceof ShadowRoot)return n}return null}function _c(){let t=typeof document<`u`&&document?document.activeElement:null;for(;t&&t.shadowRoot;){let n=t.shadowRoot.activeElement;if(n===t)break;t=n}return t}function qt(t){if(t.composedPath)try{return t.composedPath()[0]}catch{}return t.target}var kg;try{kg=typeof Intl<`u`&&Intl.v8BreakIterator}catch{kg=!1}var Te=(()=>{class t{_platformId=f(Ui);isBrowser=this._platformId?zx(this._platformId):typeof document==`object`&&!!document;EDGE=this.isBrowser&&/(edge)/i.test(navigator.userAgent);TRIDENT=this.isBrowser&&/(msie|trident)/i.test(navigator.userAgent);BLINK=this.isBrowser&&!!(window.chrome||kg)&&typeof CSS<`u`&&!this.EDGE&&!this.TRIDENT;WEBKIT=this.isBrowser&&/AppleWebKit/i.test(navigator.userAgent)&&!this.BLINK&&!this.EDGE&&!this.TRIDENT;IOS=this.isBrowser&&/iPad|iPhone|iPod/.test(navigator.userAgent)&&!(`MSStream`in window);FIREFOX=this.isBrowser&&/(firefox|minefield)/i.test(navigator.userAgent);ANDROID=this.isBrowser&&/android/i.test(navigator.userAgent)&&!this.TRIDENT;SAFARI=this.isBrowser&&/safari/i.test(navigator.userAgent)&&this.WEBKIT;static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var xc;function yC(){if(xc==null&&typeof window<`u`)try{window.addEventListener(`test`,null,Object.defineProperty({},"passive",{get:()=>xc=!0}))}finally{xc=xc||!1}return xc}function Tr(t){return yC()?t:!!t.capture}function ri(t,n=0){return _C(t)?Number(t):arguments.length===2?n:0}function _C(t){return!isNaN(parseFloat(t))&&!isNaN(Number(t))}function sn(t){return t instanceof G?t.nativeElement:t}var xC=new D(`cdk-input-modality-detector-options`);var CC={ignoreKeys:[18,17,224,91,16]};var SC=650;var Ig={passive:!0,capture:!0};var wC=(()=>{class t{_platform=f(Te);_listenerCleanups;modalityDetected;modalityChanged;get mostRecentModality(){return this._modality.value}_mostRecentTarget=null;_modality=new hi(null);_options;_lastTouchMs=0;_onKeydown=e=>{this._options?.ignoreKeys?.some(i=>i===e.keyCode)||(this._modality.next(`keyboard`),this._mostRecentTarget=qt(e))};_onMousedown=e=>{Date.now()-this._lastTouchMs<SC||(this._modality.next(So(e)?`keyboard`:`mouse`),this._mostRecentTarget=qt(e))};_onTouchstart=e=>{if(wo(e)){this._modality.next(`keyboard`);return}this._lastTouchMs=Date.now(),this._modality.next(`touch`),this._mostRecentTarget=qt(e)};constructor(){let e=f(K),i=f(ce),a=f(xC,{optional:!0});if(this._options=r(r({},CC),a),this.modalityDetected=this._modality.pipe(Ka(1)),this.modalityChanged=this.modalityDetected.pipe(Al()),this._platform.isBrowser){let o=f(kt).createRenderer(null,null);this._listenerCleanups=e.runOutsideAngular(()=>[o.listen(i,`keydown`,this._onKeydown,Ig),o.listen(i,`mousedown`,this._onMousedown,Ig),o.listen(i,`touchstart`,this._onTouchstart,Ig)])}}ngOnDestroy(){this._modality.complete(),this._listenerCleanups?.forEach(e=>e())}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var Cc=(function(t){return t[t.IMMEDIATE=0]=`IMMEDIATE`,t[t.EVENTUAL=1]=`EVENTUAL`,t})(Cc||{});var EC=new D(`cdk-focus-monitor-default-options`);var yu=Tr({passive:!0,capture:!0});var Xt=(()=>{class t{_ngZone=f(K);_platform=f(Te);_inputModalityDetector=f(wC);_origin=null;_lastFocusOrigin=null;_windowFocused=!1;_windowFocusTimeoutId;_originTimeoutId;_originFromTouchInteraction=!1;_elementInfo=new Map;_monitoredElementCount=0;_rootNodeFocusListenerCount=new Map;_detectionMode;_windowFocusListener=()=>{this._windowFocused=!0,this._windowFocusTimeoutId=setTimeout(()=>this._windowFocused=!1)};_document=f(ce);_stopInputModalityDetector=new L;constructor(){let e=f(EC,{optional:!0});this._detectionMode=e?.detectionMode||Cc.IMMEDIATE}_rootNodeFocusAndBlurListener=e=>{let i=qt(e);for(let a=i;a;a=a.parentElement)e.type===`focus`?this._onFocus(e,a):this._onBlur(e,a)};monitor(e,i=!1){let a=sn(e);if(!this._platform.isBrowser||a.nodeType!==1)return In();let o=Dg(a)||this._document,r=this._elementInfo.get(a);if(r)return i&&(r.checkChildren=!0),r.subject;let s={checkChildren:i,subject:new L,rootNode:o};return this._elementInfo.set(a,s),this._registerGlobalListeners(s),s.subject}stopMonitoring(e){let i=sn(e),a=this._elementInfo.get(i);a&&(a.subject.complete(),this._setClasses(i),this._elementInfo.delete(i),this._removeGlobalListeners(a))}focusVia(e,i,a){let o=sn(e);o===this._document.activeElement?this._getClosestElementsInfo(o).forEach(([s,c])=>this._originChanged(s,i,c)):(this._setOrigin(i),typeof o.focus==`function`&&o.focus(a))}ngOnDestroy(){this._elementInfo.forEach((e,i)=>this.stopMonitoring(i))}_getWindow(){return this._document.defaultView||window}_getFocusOrigin(e){return this._origin?this._originFromTouchInteraction?this._shouldBeAttributedToTouch(e)?`touch`:`program`:this._origin:this._windowFocused&&this._lastFocusOrigin?this._lastFocusOrigin:e&&this._isLastInteractionFromInputLabel(e)?`mouse`:`program`}_shouldBeAttributedToTouch(e){return this._detectionMode===Cc.EVENTUAL||!!e?.contains(this._inputModalityDetector._mostRecentTarget)}_setClasses(e,i){e.classList.toggle(`cdk-focused`,!!i),e.classList.toggle(`cdk-touch-focused`,i===`touch`),e.classList.toggle(`cdk-keyboard-focused`,i===`keyboard`),e.classList.toggle(`cdk-mouse-focused`,i===`mouse`),e.classList.toggle(`cdk-program-focused`,i===`program`)}_setOrigin(e,i=!1){this._ngZone.runOutsideAngular(()=>{if(this._origin=e,this._originFromTouchInteraction=e===`touch`&&i,this._detectionMode===Cc.IMMEDIATE){clearTimeout(this._originTimeoutId);let a=this._originFromTouchInteraction?SC:1;this._originTimeoutId=setTimeout(()=>this._origin=null,a)}})}_onFocus(e,i){let a=this._elementInfo.get(i),o=qt(e);!a||!a.checkChildren&&i!==o||this._originChanged(i,this._getFocusOrigin(o),a)}_onBlur(e,i){let a=this._elementInfo.get(i);!a||a.checkChildren&&e.relatedTarget instanceof Node&&i.contains(e.relatedTarget)||(this._setClasses(i),this._emitOrigin(a,null))}_emitOrigin(e,i){e.subject.observers.length&&this._ngZone.run(()=>e.subject.next(i))}_registerGlobalListeners(e){if(!this._platform.isBrowser)return;let i=e.rootNode,a=this._rootNodeFocusListenerCount.get(i)||0;a||this._ngZone.runOutsideAngular(()=>{i.addEventListener(`focus`,this._rootNodeFocusAndBlurListener,yu),i.addEventListener(`blur`,this._rootNodeFocusAndBlurListener,yu)}),this._rootNodeFocusListenerCount.set(i,a+1),++this._monitoredElementCount===1&&(this._ngZone.runOutsideAngular(()=>{this._getWindow().addEventListener(`focus`,this._windowFocusListener)}),this._inputModalityDetector.modalityDetected.pipe(Me(this._stopInputModalityDetector)).subscribe(o=>{this._setOrigin(o,!0)}))}_removeGlobalListeners(e){let i=e.rootNode;if(this._rootNodeFocusListenerCount.has(i)){let a=this._rootNodeFocusListenerCount.get(i);a>1?this._rootNodeFocusListenerCount.set(i,a-1):(i.removeEventListener(`focus`,this._rootNodeFocusAndBlurListener,yu),i.removeEventListener(`blur`,this._rootNodeFocusAndBlurListener,yu),this._rootNodeFocusListenerCount.delete(i))}--this._monitoredElementCount||(this._getWindow().removeEventListener(`focus`,this._windowFocusListener),this._stopInputModalityDetector.next(),clearTimeout(this._windowFocusTimeoutId),clearTimeout(this._originTimeoutId))}_originChanged(e,i,a){this._setClasses(e,i),this._emitOrigin(a,i),this._lastFocusOrigin=i}_getClosestElementsInfo(e){let i=[];return this._elementInfo.forEach((a,o)=>{(o===e||a.checkChildren&&o.contains(e))&&i.push([o,a])}),i}_isLastInteractionFromInputLabel(e){let{_mostRecentTarget:i,mostRecentModality:a}=this._inputModalityDetector;if(a!==`mouse`||!i||i===e||e.nodeName!==`INPUT`&&e.nodeName!==`TEXTAREA`||e.disabled)return!1;let o=e.labels;if(o){for(let r=0;r<o.length;r++)if(o[r].contains(i))return!0}return!1}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var Mg=(()=>{class t{_elementRef=f(G);_focusMonitor=f(Xt);_monitorSubscription;_focusOrigin=null;cdkFocusChange=new J;get focusOrigin(){return this._focusOrigin}ngAfterViewInit(){let e=this._elementRef.nativeElement;this._monitorSubscription=this._focusMonitor.monitor(e,e.nodeType===1&&e.hasAttribute(`cdkMonitorSubtreeFocus`)).subscribe(i=>{this._focusOrigin=i,this.cdkFocusChange.emit(i)})}ngOnDestroy(){this._focusMonitor.stopMonitoring(this._elementRef),this._monitorSubscription?.unsubscribe()}static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,selectors:[[``,`cdkMonitorElementFocus`,``],[``,`cdkMonitorSubtreeFocus`,``]],outputs:{cdkFocusChange:`cdkFocusChange`},exportAs:[`cdkMonitorFocus`]})}return t})();var _u=new WeakMap;var Ze=(()=>{class t{_appRef;_injector=f(re);_environmentInjector=f(Et);load(e){let i=this._appRef=this._appRef||this._injector.get(On),a=_u.get(i);a||(a={loaders:new Set,refs:[]},_u.set(i,a),i.onDestroy(()=>{_u.get(i)?.refs.forEach(o=>o.destroy()),_u.delete(i)})),a.loaders.has(e)||(a.loaders.add(e),a.refs.push(Xd(e,{environmentInjector:this._environmentInjector})))}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var Di=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=B({type:t,selectors:[[`ng-component`]],exportAs:[`cdkVisuallyHidden`],decls:0,vars:0,template:function(i,a){},styles:[`.cdk-visually-hidden {
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
`],encapsulation:2})}return t})();var xu;function YT(){if(xu===void 0&&(xu=null,typeof window<`u`)){let t=window;if(t.trustedTypes!==void 0)try{xu=t.trustedTypes.createPolicy(`angular#components`,{createHTML:n=>n})}catch(n){console.error(n)}}return xu}function KT(t){return YT()?.createHTML(t)||t}function DC(t,n,e){t.innerHTML=KT(e.sanitize(bn.HTML,n)||``)}function Ar(t){return Array.isArray(t)?t:[t]}var kC=new Set;var Eo;var Or=(()=>{class t{_platform=f(Te);_nonce=f(ya,{optional:!0});_matchMedia;constructor(){this._matchMedia=this._platform.isBrowser&&window.matchMedia?window.matchMedia.bind(window):XT}matchMedia(e){return(this._platform.WEBKIT||this._platform.BLINK)&&QT(e,this._nonce),this._matchMedia(e)}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();function QT(t,n){if(!kC.has(t))try{Eo||(Eo=document.createElement(`style`),n&&Eo.setAttribute(`nonce`,n),Eo.setAttribute(`type`,`text/css`),document.head.appendChild(Eo)),Eo.sheet&&(Eo.sheet.insertRule(`@media ${t.replace(/[{}]/g,``)} {body{ }}`,0),kC.add(t))}catch(e){console.error(e)}}function XT(t){return{matches:t===`all`||t===``,media:t,addListener:()=>{},removeListener:()=>{}}}var Ng=(()=>{class t{_mediaMatcher=f(Or);_zone=f(K);_queries=new Map;_destroySubject=new L;ngOnDestroy(){this._destroySubject.next(),this._destroySubject.complete()}isMatched(e){return IC(Ar(e)).some(a=>this._registerQuery(a).mql.matches)}observe(e){let o=ep(IC(Ar(e)).map(r=>this._registerQuery(r).observable));return o=Xo(o.pipe(hn(1)),o.pipe(Ka(1),Ri(0))),o.pipe(ot(r=>{let s={matches:!1,breakpoints:{}};return r.forEach(({matches:c,query:m})=>{s.matches=s.matches||c,s.breakpoints[m]=c}),s}))}_registerQuery(e){if(this._queries.has(e))return this._queries.get(e);let i=this._mediaMatcher.matchMedia(e),o={observable:new xe(r=>{let s=c=>this._zone.run(()=>r.next(c));return i.addListener(s),()=>{i.removeListener(s)}}).pipe(dt(i),ot(({matches:r})=>({query:e,matches:r})),Me(this._destroySubject)),mql:i};return this._queries.set(e,o),o}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();function IC(t){return t.map(n=>n.split(`,`)).reduce((n,e)=>n.concat(e)).map(n=>n.trim())}function ZT(t){if(t.type===`characterData`&&t.target instanceof Comment)return!0;if(t.type===`childList`){for(let n=0;n<t.addedNodes.length;n++)if(!(t.addedNodes[n]instanceof Comment))return!1;for(let n=0;n<t.removedNodes.length;n++)if(!(t.removedNodes[n]instanceof Comment))return!1;return!0}return!1}var MC=(()=>{class t{create(e){return typeof MutationObserver>`u`?null:new MutationObserver(e)}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var NC=(()=>{class t{_mutationObserverFactory=f(MC);_observedElements=new Map;_ngZone=f(K);ngOnDestroy(){this._observedElements.forEach((e,i)=>this._cleanupObserver(i))}observe(e){let i=sn(e);return new xe(a=>{let r=this._observeElement(i).pipe(ot(s=>s.filter(c=>!ZT(c))),Ke(s=>!!s.length)).subscribe(s=>{this._ngZone.run(()=>{a.next(s)})});return()=>{r.unsubscribe(),this._unobserveElement(i)}})}_observeElement(e){return this._ngZone.runOutsideAngular(()=>{if(this._observedElements.has(e))this._observedElements.get(e).count++;else{let i=new L,a=this._mutationObserverFactory.create(o=>i.next(o));a&&a.observe(e,{characterData:!0,childList:!0,subtree:!0}),this._observedElements.set(e,{observer:a,stream:i,count:1})}return this._observedElements.get(e).stream})}_unobserveElement(e){this._observedElements.has(e)&&(this._observedElements.get(e).count--,this._observedElements.get(e).count||this._cleanupObserver(e))}_cleanupObserver(e){if(this._observedElements.has(e)){let{observer:i,stream:a}=this._observedElements.get(e);i&&i.disconnect(),a.complete(),this._observedElements.delete(e)}}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var TC=(()=>{class t{_contentObserver=f(NC);_elementRef=f(G);event=new J;get disabled(){return this._disabled}set disabled(e){this._disabled=e,this._disabled?this._unsubscribe():this._subscribe()}_disabled=!1;get debounce(){return this._debounce}set debounce(e){this._debounce=ri(e),this._subscribe()}_debounce;_currentSubscription=null;ngAfterContentInit(){!this._currentSubscription&&!this.disabled&&this._subscribe()}ngOnDestroy(){this._unsubscribe()}_subscribe(){this._unsubscribe();let e=this._contentObserver.observe(this._elementRef);this._currentSubscription=(this.debounce?e.pipe(Ri(this.debounce)):e).subscribe(this.event)}_unsubscribe(){this._currentSubscription?.unsubscribe()}static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,selectors:[[``,`cdkObserveContent`,``]],inputs:{disabled:[2,`cdkObserveContentDisabled`,`disabled`,V],debounce:`debounce`},outputs:{event:`cdkObserveContent`},exportAs:[`cdkObserveContent`]})}return t})();var Cu=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({providers:[MC]})}return t})();var Rr=(()=>{class t{_platform=f(Te);isDisabled(e){return e.hasAttribute(`disabled`)}isVisible(e){return eA(e)&&getComputedStyle(e).visibility===`visible`}isTabbable(e){if(!this._platform.isBrowser)return!1;let i=JT(cA(e));if(i&&(AC(i)===-1||!this.isVisible(i)))return!1;let a=e.nodeName.toLowerCase(),o=AC(e);return e.hasAttribute(`contenteditable`)?o!==-1:a===`iframe`||a===`object`||this._platform.WEBKIT&&this._platform.IOS&&!rA(e)?!1:a===`audio`?e.hasAttribute(`controls`)?o!==-1:!1:a===`video`?o===-1?!1:o!==null?!0:this._platform.FIREFOX||e.hasAttribute(`controls`):e.tabIndex>=0}isFocusable(e,i){return sA(e)&&!this.isDisabled(e)&&(i?.ignoreVisibility||this.isVisible(e))}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();function JT(t){try{return t.frameElement}catch{return null}}function eA(t){return!!(t.offsetWidth||t.offsetHeight||typeof t.getClientRects==`function`&&t.getClientRects().length)}function tA(t){let n=t.nodeName.toLowerCase();return n===`input`||n===`select`||n===`button`||n===`textarea`}function nA(t){return aA(t)&&t.type==`hidden`}function iA(t){return oA(t)&&t.hasAttribute(`href`)}function aA(t){return t.nodeName.toLowerCase()==`input`}function oA(t){return t.nodeName.toLowerCase()==`a`}function PC(t){if(!t.hasAttribute(`tabindex`)||t.tabIndex===void 0)return!1;let n=t.getAttribute(`tabindex`);return!!(n&&!isNaN(parseInt(n,10)))}function AC(t){if(!PC(t))return null;let n=parseInt(t.getAttribute(`tabindex`)||``,10);return isNaN(n)?-1:n}function rA(t){let n=t.nodeName.toLowerCase(),e=n===`input`&&t.type;return e===`text`||e===`password`||n===`select`||n===`textarea`}function sA(t){return nA(t)?!1:tA(t)||iA(t)||t.hasAttribute(`contenteditable`)||PC(t)}function cA(t){return t.ownerDocument&&t.ownerDocument.defaultView||window}var Su=class{_element;_checker;_ngZone;_document;_injector;_startAnchor=null;_endAnchor=null;_hasAttached=!1;startAnchorListener=()=>{!this.focusLastTabbableElement()&&this._checker.isFocusable(this._element)&&this._element.focus()};endAnchorListener=()=>{!this.focusFirstTabbableElement()&&this._checker.isFocusable(this._element)&&this._element.focus()};get enabled(){return this._enabled}set enabled(n){this._enabled=n,this._startAnchor&&this._endAnchor&&(this._toggleAnchorTabIndex(n,this._startAnchor),this._toggleAnchorTabIndex(n,this._endAnchor))}_enabled=!0;constructor(n,e,i,a,o=!1,r){this._element=n,this._checker=e,this._ngZone=i,this._document=a,this._injector=r,o||this.attachAnchors()}destroy(){let n=this._startAnchor,e=this._endAnchor;n&&(n.removeEventListener(`focus`,this.startAnchorListener),n.remove()),e&&(e.removeEventListener(`focus`,this.endAnchorListener),e.remove()),this._startAnchor=this._endAnchor=null,this._hasAttached=!1}attachAnchors(){return this._hasAttached?!0:(this._ngZone.runOutsideAngular(()=>{this._startAnchor||(this._startAnchor=this._createAnchor(),this._startAnchor.addEventListener(`focus`,this.startAnchorListener)),this._endAnchor||(this._endAnchor=this._createAnchor(),this._endAnchor.addEventListener(`focus`,this.endAnchorListener))}),this._element.parentNode&&(this._element.parentNode.insertBefore(this._startAnchor,this._element),this._element.parentNode.insertBefore(this._endAnchor,this._element.nextSibling),this._hasAttached=!0),this._hasAttached)}focusInitialElementWhenReady(n){return new Promise(e=>{this._executeOnStable(()=>e(this.focusInitialElement(n)))})}focusFirstTabbableElementWhenReady(n){return new Promise(e=>{this._executeOnStable(()=>e(this.focusFirstTabbableElement(n)))})}focusLastTabbableElementWhenReady(n){return new Promise(e=>{this._executeOnStable(()=>e(this.focusLastTabbableElement(n)))})}_getRegionBoundary(n){let e=this._element.querySelectorAll(`[cdk-focus-region-${n}], [cdkFocusRegion${n}], [cdk-focus-${n}]`);return n==`start`?e.length?e[0]:this._getFirstTabbableElement(this._element):e.length?e[e.length-1]:this._getLastTabbableElement(this._element)}focusInitialElement(n){let e=this._element.querySelector(`[cdk-focus-initial], [cdkFocusInitial]`);if(e){if(!this._checker.isFocusable(e)){let i=this._getFirstTabbableElement(e);return i?.focus(n),!!i}return e.focus(n),!0}return this.focusFirstTabbableElement(n)}focusFirstTabbableElement(n){let e=this._getRegionBoundary(`start`);return e&&e.focus(n),!!e}focusLastTabbableElement(n){let e=this._getRegionBoundary(`end`);return e&&e.focus(n),!!e}hasAttached(){return this._hasAttached}_getFirstTabbableElement(n){if(this._checker.isFocusable(n)&&this._checker.isTabbable(n))return n;let e=n.children;for(let i=0;i<e.length;i++){let a=e[i].nodeType===this._document.ELEMENT_NODE?this._getFirstTabbableElement(e[i]):null;if(a)return a}return null}_getLastTabbableElement(n){if(this._checker.isFocusable(n)&&this._checker.isTabbable(n))return n;let e=n.children;for(let i=e.length-1;i>=0;i--){let a=e[i].nodeType===this._document.ELEMENT_NODE?this._getLastTabbableElement(e[i]):null;if(a)return a}return null}_createAnchor(){let n=this._document.createElement(`div`);return this._toggleAnchorTabIndex(this._enabled,n),n.classList.add(`cdk-visually-hidden`),n.classList.add(`cdk-focus-trap-anchor`),n.setAttribute(`aria-hidden`,`true`),n}_toggleAnchorTabIndex(n,e){n?e.setAttribute(`tabindex`,`0`):e.removeAttribute(`tabindex`)}toggleAnchors(n){this._startAnchor&&this._endAnchor&&(this._toggleAnchorTabIndex(n,this._startAnchor),this._toggleAnchorTabIndex(n,this._endAnchor))}_executeOnStable(n){Mt(n,{injector:this._injector})}};var Ag=(()=>{class t{_checker=f(Rr);_ngZone=f(K);_document=f(ce);_injector=f(re);constructor(){f(Ze).load(Di)}create(e,i=!1){return new Su(e,this._checker,this._ngZone,this._document,i,this._injector)}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var FC=new D(`liveAnnouncerElement`,{providedIn:`root`,factory:()=>null});var LC=new D(`LIVE_ANNOUNCER_DEFAULT_OPTIONS`);var lA=0;var Og=(()=>{class t{_ngZone=f(K);_defaultOptions=f(LC,{optional:!0});_liveElement;_document=f(ce);_sanitizer=f(wg);_previousTimeout;_currentPromise;_currentResolve;constructor(){let e=f(FC,{optional:!0});this._liveElement=e||this._createLiveElement()}announce(e,...i){let a=this._defaultOptions,o,r;return i.length===1&&typeof i[0]==`number`?r=i[0]:[o,r]=i,this.clear(),clearTimeout(this._previousTimeout),o||(o=a&&a.politeness?a.politeness:`polite`),r==null&&a&&(r=a.duration),this._liveElement.setAttribute(`aria-live`,o),this._liveElement.id&&this._exposeAnnouncerToModals(this._liveElement.id),this._ngZone.runOutsideAngular(()=>(this._currentPromise||(this._currentPromise=new Promise(s=>this._currentResolve=s)),clearTimeout(this._previousTimeout),this._previousTimeout=setTimeout(()=>{!e||typeof e==`string`?this._liveElement.textContent=e:DC(this._liveElement,e,this._sanitizer),typeof r==`number`&&(this._previousTimeout=setTimeout(()=>this.clear(),r)),this._currentResolve?.(),this._currentPromise=this._currentResolve=void 0},100),this._currentPromise))}clear(){this._liveElement&&(this._liveElement.textContent=``)}ngOnDestroy(){clearTimeout(this._previousTimeout),this._liveElement?.remove(),this._liveElement=null,this._currentResolve?.(),this._currentPromise=this._currentResolve=void 0}_createLiveElement(){let e=`cdk-live-announcer-element`,i=this._document.getElementsByClassName(e),a=this._document.createElement(`div`);for(let o=0;o<i.length;o++)i[o].remove();return a.classList.add(e),a.classList.add(`cdk-visually-hidden`),a.setAttribute(`aria-atomic`,`true`),a.setAttribute(`aria-live`,`polite`),a.id=`cdk-live-announcer-${lA++}`,this._document.body.appendChild(a),a}_exposeAnnouncerToModals(e){let i=this._document.querySelectorAll(`body > .cdk-overlay-container [aria-modal="true"]`);for(let a=0;a<i.length;a++){let o=i[a],r=o.getAttribute(`aria-owns`);r?r.indexOf(e)===-1&&o.setAttribute(`aria-owns`,r+` `+e):o.setAttribute(`aria-owns`,e)}}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var Ea=(function(t){return t[t.NONE=0]=`NONE`,t[t.BLACK_ON_WHITE=1]=`BLACK_ON_WHITE`,t[t.WHITE_ON_BLACK=2]=`WHITE_ON_BLACK`,t})(Ea||{});var OC=`cdk-high-contrast-black-on-white`;var RC=`cdk-high-contrast-white-on-black`;var Tg=`cdk-high-contrast-active`;var VC=(()=>{class t{_platform=f(Te);_hasCheckedHighContrastMode=!1;_document=f(ce);_breakpointSubscription;constructor(){this._breakpointSubscription=f(Ng).observe(`(forced-colors: active)`).subscribe(()=>{this._hasCheckedHighContrastMode&&(this._hasCheckedHighContrastMode=!1,this._applyBodyHighContrastModeCssClasses())})}getHighContrastMode(){if(!this._platform.isBrowser)return Ea.NONE;let e=this._document.createElement(`div`);e.style.backgroundColor=`rgb(1,2,3)`,e.style.position=`absolute`,this._document.body.appendChild(e);let i=this._document.defaultView||window,a=i&&i.getComputedStyle?i.getComputedStyle(e):null,o=(a&&a.backgroundColor||``).replace(/ /g,``);switch(e.remove(),o){case`rgb(0,0,0)`:case`rgb(45,50,54)`:case`rgb(32,32,32)`:return Ea.WHITE_ON_BLACK;case`rgb(255,255,255)`:case`rgb(255,250,239)`:return Ea.BLACK_ON_WHITE}return Ea.NONE}ngOnDestroy(){this._breakpointSubscription.unsubscribe()}_applyBodyHighContrastModeCssClasses(){if(!this._hasCheckedHighContrastMode&&this._platform.isBrowser&&this._document.body){let e=this._document.body.classList;e.remove(Tg,OC,RC),this._hasCheckedHighContrastMode=!0;let i=this.getHighContrastMode();i===Ea.BLACK_ON_WHITE?e.add(Tg,OC):i===Ea.WHITE_ON_BLACK&&e.add(Tg,RC)}}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var Do=(()=>{class t{constructor(){f(VC)._applyBodyHighContrastModeCssClasses()}static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({imports:[Cu]})}return t})();var dA=200;var wu=class{_letterKeyStream=new L;_items=[];_selectedItemIndex=-1;_pressedLetters=[];_skipPredicateFn;_selectedItem=new L;selectedItem=this._selectedItem;constructor(n,e){let i=typeof e?.debounceInterval==`number`?e.debounceInterval:dA;e?.skipPredicate&&(this._skipPredicateFn=e.skipPredicate),this.setItems(n),this._setupKeyHandler(i)}destroy(){this._pressedLetters=[],this._letterKeyStream.complete(),this._selectedItem.complete()}setCurrentSelectedItemIndex(n){this._selectedItemIndex=n}setItems(n){this._items=n}handleKey(n){let e=n.keyCode;n.key&&n.key.length===1?this._letterKeyStream.next(n.key.toLocaleUpperCase()):(e>=65&&e<=90||e>=48&&e<=57)&&this._letterKeyStream.next(String.fromCharCode(e))}isTyping(){return this._pressedLetters.length>0}reset(){this._pressedLetters=[]}_setupKeyHandler(n){this._letterKeyStream.pipe(rp(e=>this._pressedLetters.push(e)),Ri(n),Ke(()=>this._pressedLetters.length>0),ot(()=>this._pressedLetters.join(``).toLocaleUpperCase())).subscribe(e=>{for(let i=1;i<this._items.length+1;i++){let a=(this._selectedItemIndex+i)%this._items.length,o=this._items[a];if(!this._skipPredicateFn?.(o)&&o.getLabel?.().toLocaleUpperCase().trim().indexOf(e)===0){this._selectedItem.next(o);break}}this._pressedLetters=[]})}};function vt(t,...n){return n.length?n.some(e=>t[e]):t.altKey||t.shiftKey||t.ctrlKey||t.metaKey}var Pr=class{_items;_activeItemIndex=M(-1);_activeItem=M(null);_wrap=!1;_typeaheadSubscription=De.EMPTY;_itemChangesSubscription;_vertical=!0;_horizontal=null;_allowedModifierKeys=[];_homeAndEnd=!1;_pageUpAndDown={enabled:!1,delta:10};_effectRef;_typeahead;_skipPredicateFn=n=>n.disabled;constructor(n,e){this._items=n,n instanceof _n?this._itemChangesSubscription=n.changes.subscribe(i=>this._itemsChanged(i.toArray())):on(n)&&(this._effectRef=Pt(()=>this._itemsChanged(n()),{injector:e}))}tabOut=new L;change=new L;skipPredicate(n){return this._skipPredicateFn=n,this}withWrap(n=!0){return this._wrap=n,this}withVerticalOrientation(n=!0){return this._vertical=n,this}withHorizontalOrientation(n){return this._horizontal=n,this}withAllowedModifierKeys(n){return this._allowedModifierKeys=n,this}withTypeAhead(n=200){this._typeaheadSubscription.unsubscribe();let e=this._getItemsArray();return this._typeahead=new wu(e,{debounceInterval:typeof n==`number`?n:void 0,skipPredicate:i=>this._skipPredicateFn(i)}),this._typeaheadSubscription=this._typeahead.selectedItem.subscribe(i=>{this.setActiveItem(i)}),this}cancelTypeahead(){return this._typeahead?.reset(),this}withHomeAndEnd(n=!0){return this._homeAndEnd=n,this}withPageUpDown(n=!0,e=10){return this._pageUpAndDown={enabled:n,delta:e},this}setActiveItem(n){let e=this._activeItem();this.updateActiveItem(n),this._activeItem()!==e&&this.change.next(this._activeItemIndex())}onKeydown(n){let e=n.keyCode,a=[`altKey`,`ctrlKey`,`metaKey`,`shiftKey`].every(o=>!n[o]||this._allowedModifierKeys.indexOf(o)>-1);switch(e){case 9:this.tabOut.next();return;case 40:if(this._vertical&&a){this.setNextItemActive();break}else return;case 38:if(this._vertical&&a){this.setPreviousItemActive();break}else return;case 39:if(this._horizontal&&a){this._horizontal===`rtl`?this.setPreviousItemActive():this.setNextItemActive();break}else return;case 37:if(this._horizontal&&a){this._horizontal===`rtl`?this.setNextItemActive():this.setPreviousItemActive();break}else return;case 36:if(this._homeAndEnd&&a){this.setFirstItemActive();break}else return;case 35:if(this._homeAndEnd&&a){this.setLastItemActive();break}else return;case 33:if(this._pageUpAndDown.enabled&&a){let o=this._activeItemIndex()-this._pageUpAndDown.delta;this._setActiveItemByIndex(o>0?o:0,1);break}else return;case 34:if(this._pageUpAndDown.enabled&&a){let o=this._activeItemIndex()+this._pageUpAndDown.delta,r=this._getItemsArray().length;this._setActiveItemByIndex(o<r?o:r-1,-1);break}else return;default:(a||vt(n,`shiftKey`))&&this._typeahead?.handleKey(n);return}this._typeahead?.reset(),n.preventDefault()}get activeItemIndex(){return this._activeItemIndex()}get activeItem(){return this._activeItem()}isTyping(){return!!this._typeahead&&this._typeahead.isTyping()}setFirstItemActive(){this._setActiveItemByIndex(0,1)}setLastItemActive(){this._setActiveItemByIndex(this._getItemsArray().length-1,-1)}setNextItemActive(){this._activeItemIndex()<0?this.setFirstItemActive():this._setActiveItemByDelta(1)}setPreviousItemActive(){this._activeItemIndex()<0&&this._wrap?this.setLastItemActive():this._setActiveItemByDelta(-1)}updateActiveItem(n){let e=this._getItemsArray(),i=typeof n==`number`?n:e.indexOf(n),a=e[i];this._activeItem.set(a??null),this._activeItemIndex.set(i),this._typeahead?.setCurrentSelectedItemIndex(i)}destroy(){this._typeaheadSubscription.unsubscribe(),this._itemChangesSubscription?.unsubscribe(),this._effectRef?.destroy(),this._typeahead?.destroy(),this.tabOut.complete(),this.change.complete()}_setActiveItemByDelta(n){this._wrap?this._setActiveInWrapMode(n):this._setActiveInDefaultMode(n)}_setActiveInWrapMode(n){let e=this._getItemsArray();for(let i=1;i<=e.length;i++){let a=(this._activeItemIndex()+n*i+e.length)%e.length,o=e[a];if(!this._skipPredicateFn(o)){this.setActiveItem(a);return}}}_setActiveInDefaultMode(n){this._setActiveItemByIndex(this._activeItemIndex()+n,n)}_setActiveItemByIndex(n,e){let i=this._getItemsArray();if(i[n]){for(;this._skipPredicateFn(i[n]);)if(n+=e,!i[n])return;this.setActiveItem(n)}}_getItemsArray(){return on(this._items)?this._items():this._items instanceof _n?this._items.toArray():this._items}_itemsChanged(n){this._typeahead?.setItems(n);let e=this._activeItem();if(e){let i=n.indexOf(e);i>-1&&i!==this._activeItemIndex()&&(this._activeItemIndex.set(i),this._typeahead?.setCurrentSelectedItemIndex(i))}}};var Sc=class extends Pr{setActiveItem(n){this.activeItem&&this.activeItem.setInactiveStyles(),super.setActiveItem(n),this.activeItem&&this.activeItem.setActiveStyles()}};var Ji=class extends Pr{_origin=`program`;setFocusOrigin(n){return this._origin=n,this}setActiveItem(n){super.setActiveItem(n),this.activeItem&&this.activeItem.focus(this._origin)}};var jC=new Map;var Le=class t{_appId=f(Hi);static _infix=`a${Math.floor(Math.random()*1e5).toString()}`;getId(n,e=!1){this._appId!==`ng`&&(n+=this._appId);let i=jC.get(n);return i===void 0?i=0:i++,jC.set(n,i),`${n}${e?t._infix+`-`:``}${i}`}static ɵfac=function(e){return new(e||t)};static ɵprov=se({token:t,factory:t.ɵfac})};var HC=` `;function uA(t,n,e){let i=Du(t,n);e=e.trim(),!i.some(a=>a.trim()===e)&&(i.push(e),t.setAttribute(n,i.join(HC)))}function mA(t,n,e){let i=Du(t,n);e=e.trim();let a=i.filter(o=>o!==e);a.length?t.setAttribute(n,a.join(HC)):t.removeAttribute(n)}function Du(t,n){return t.getAttribute(n)?.match(/\S+/g)??[]}var UC=`cdk-describedby-message`;var Eu=`cdk-describedby-host`;var Pg=0;var ku=(()=>{class t{_platform=f(Te);_document=f(ce);_messageRegistry=new Map;_messagesContainer=null;_id=`${Pg++}`;constructor(){f(Ze).load(Di),this._id=f(Hi)+`-`+Pg++}describe(e,i,a){if(!this._canBeDescribed(e,i))return;let o=Rg(i,a);typeof i!=`string`?(zC(i,this._id),this._messageRegistry.set(o,{messageElement:i,referenceCount:0})):this._messageRegistry.has(o)||this._createMessageElement(i,a),this._isElementDescribedByMessage(e,o)||this._addMessageReference(e,o)}removeDescription(e,i,a){if(!i||!this._isElementNode(e))return;let o=Rg(i,a);if(this._isElementDescribedByMessage(e,o)&&this._removeMessageReference(e,o),typeof i==`string`){let r=this._messageRegistry.get(o);r&&r.referenceCount===0&&this._deleteMessageElement(o)}this._messagesContainer?.childNodes.length===0&&(this._messagesContainer.remove(),this._messagesContainer=null)}ngOnDestroy(){let e=this._document.querySelectorAll(`[${Eu}="${this._id}"]`);for(let i=0;i<e.length;i++)this._removeCdkDescribedByReferenceIds(e[i]),e[i].removeAttribute(Eu);this._messagesContainer?.remove(),this._messagesContainer=null,this._messageRegistry.clear()}_createMessageElement(e,i){let a=this._document.createElement(`div`);zC(a,this._id),a.textContent=e,i&&a.setAttribute(`role`,i),this._createMessagesContainer(),this._messagesContainer.appendChild(a),this._messageRegistry.set(Rg(e,i),{messageElement:a,referenceCount:0})}_deleteMessageElement(e){this._messageRegistry.get(e)?.messageElement?.remove(),this._messageRegistry.delete(e)}_createMessagesContainer(){if(this._messagesContainer)return;let e=`cdk-describedby-message-container`,i=this._document.querySelectorAll(`.${e}[platform="server"]`);for(let o=0;o<i.length;o++)i[o].remove();let a=this._document.createElement(`div`);a.style.visibility=`hidden`,a.classList.add(e),a.classList.add(`cdk-visually-hidden`),this._platform.isBrowser||a.setAttribute(`platform`,`server`),this._document.body.appendChild(a),this._messagesContainer=a}_removeCdkDescribedByReferenceIds(e){let i=Du(e,`aria-describedby`).filter(a=>a.indexOf(UC)!=0);e.setAttribute(`aria-describedby`,i.join(` `))}_addMessageReference(e,i){let a=this._messageRegistry.get(i);uA(e,`aria-describedby`,a.messageElement.id),e.setAttribute(Eu,this._id),a.referenceCount++}_removeMessageReference(e,i){let a=this._messageRegistry.get(i);a.referenceCount--,mA(e,`aria-describedby`,a.messageElement.id),e.removeAttribute(Eu)}_isElementDescribedByMessage(e,i){let a=Du(e,`aria-describedby`),o=this._messageRegistry.get(i),r=o&&o.messageElement.id;return!!r&&a.indexOf(r)!=-1}_canBeDescribed(e,i){if(!this._isElementNode(e))return!1;if(i&&typeof i==`object`)return!0;let a=i==null?``:`${i}`.trim(),o=e.getAttribute(`aria-label`);return a?!o||o.trim()!==a:!1}_isElementNode(e){return e.nodeType===this._document.ELEMENT_NODE}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();function Rg(t,n){return typeof t==`string`?`${n||``}/${t}`:t}function zC(t,n){t.id||(t.id=`${UC}-${n}-${Pg++}`)}var si=(function(t){return t[t.NORMAL=0]=`NORMAL`,t[t.NEGATED=1]=`NEGATED`,t[t.INVERTED=2]=`INVERTED`,t})(si||{});var Iu;var To;function Mu(){if(To==null){if(typeof document!=`object`||!document||typeof Element!=`function`||!Element)return To=!1,To;if(document.documentElement?.style&&`scrollBehavior`in document.documentElement.style)To=!0;else{let t=Element.prototype.scrollTo;t?To=!/\{\s*\[native code\]\s*\}/.test(t.toString()):To=!1}}return To}function Fr(){if(typeof document!=`object`||!document)return si.NORMAL;if(Iu==null){let t=document.createElement(`div`),n=t.style;t.dir=`rtl`,n.width=`1px`,n.overflow=`auto`,n.visibility=`hidden`,n.pointerEvents=`none`,n.position=`absolute`;let e=document.createElement(`div`),i=e.style;i.width=`2px`,i.height=`1px`,t.appendChild(e),document.body.appendChild(t),Iu=si.NORMAL,t.scrollLeft===0&&(t.scrollLeft=1,Iu=t.scrollLeft===0?si.NEGATED:si.INVERTED),t.remove()}return Iu}function Fg(){return typeof __karma__<`u`&&!!__karma__||typeof jasmine<`u`&&!!jasmine||typeof jest<`u`&&!!jest||typeof Mocha<`u`&&!!Mocha}var Lr;var $C=[`color`,`button`,`checkbox`,`date`,`datetime-local`,`email`,`file`,`hidden`,`image`,`month`,`number`,`password`,`radio`,`range`,`reset`,`search`,`submit`,`tel`,`text`,`time`,`url`,`week`];function Lg(){if(Lr)return Lr;if(typeof document!=`object`||!document)return Lr=new Set($C),Lr;let t=document.createElement(`input`);return Lr=new Set($C.filter(n=>(t.setAttribute(`type`,n),t.type===n))),Lr}var pA=new D(`MATERIAL_ANIMATIONS`);var GC=null;function wc(){return f(pA,{optional:!0})?.animationsDisabled||f(Fs,{optional:!0})===`NoopAnimations`?`di-disabled`:(GC??=f(Or).matchMedia(`(prefers-reduced-motion)`).matches,GC?`reduced-motion`:`enabled`)}function ze(){return wc()!==`enabled`}function St(t){return t==null?``:typeof t==`string`?t:`${t}px`}function ea(t){return t!=null&&`${t}`!=`false`}var Ln=(function(t){return t[t.FADING_IN=0]=`FADING_IN`,t[t.VISIBLE=1]=`VISIBLE`,t[t.FADING_OUT=2]=`FADING_OUT`,t[t.HIDDEN=3]=`HIDDEN`,t})(Ln||{});var Vg=class{_renderer;element;config;_animationForciblyDisabledThroughCss;state=Ln.HIDDEN;constructor(n,e,i,a=!1){this._renderer=n,this.element=e,this.config=i,this._animationForciblyDisabledThroughCss=a}fadeOut(){this._renderer.fadeOutRipple(this)}};var WC=Tr({passive:!0,capture:!0});var Bg=class{_events=new Map;addHandler(n,e,i,a){let o=this._events.get(e);if(o){let r=o.get(i);r?r.add(a):o.set(i,new Set([a]))}else this._events.set(e,new Map([[i,new Set([a])]])),n.runOutsideAngular(()=>{document.addEventListener(e,this._delegateEventHandler,WC)})}removeHandler(n,e,i){let a=this._events.get(n);if(!a)return;let o=a.get(e);o&&(o.delete(i),o.size===0&&a.delete(e),a.size===0&&(this._events.delete(n),document.removeEventListener(n,this._delegateEventHandler,WC)))}_delegateEventHandler=n=>{let e=qt(n);e&&this._events.get(n.type)?.forEach((i,a)=>{(a===e||a.contains(e))&&i.forEach(o=>o.handleEvent(n))})}};var Ec={enterDuration:225,exitDuration:150};var hA=800;var qC=Tr({passive:!0,capture:!0});var YC=[`mousedown`,`touchstart`];var KC=[`mouseup`,`mouseleave`,`touchend`,`touchcancel`];var fA=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=B({type:t,selectors:[[`ng-component`]],hostAttrs:[`mat-ripple-style-loader`,``],decls:0,vars:0,template:function(i,a){},styles:[`.mat-ripple {
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
`],encapsulation:2})}return t})();var Dc=class t{_target;_ngZone;_platform;_containerElement;_triggerElement=null;_isPointerDown=!1;_activeRipples=new Map;_mostRecentTransientRipple=null;_lastTouchStartEvent;_pointerUpEventsRegistered=!1;_containerRect=null;static _eventManager=new Bg;constructor(n,e,i,a,o){this._target=n,this._ngZone=e,this._platform=a,a.isBrowser&&(this._containerElement=sn(i)),o&&o.get(Ze).load(fA)}fadeInRipple(n,e,i={}){let a=this._containerRect=this._containerRect||this._containerElement.getBoundingClientRect(),o=r(r({},Ec),i.animation);i.centered&&(n=a.left+a.width/2,e=a.top+a.height/2);let r$2=i.radius||gA(n,e,a),s=n-a.left,c=e-a.top,m=o.enterDuration,u=document.createElement(`div`);u.classList.add(`mat-ripple-element`),u.style.left=`${s-r$2}px`,u.style.top=`${c-r$2}px`,u.style.height=`${r$2*2}px`,u.style.width=`${r$2*2}px`,i.color!=null&&(u.style.backgroundColor=i.color),u.style.transitionDuration=`${m}ms`,this._containerElement.appendChild(u);let g=window.getComputedStyle(u),v=g.transitionProperty,b=g.transitionDuration,y=v===`none`||b===`0s`||b===`0s, 0s`||a.width===0&&a.height===0,E=new Vg(this,u,i,y);u.style.transform=`scale3d(1, 1, 1)`,E.state=Ln.FADING_IN,i.persistent||(this._mostRecentTransientRipple=E);let R=null;return!y&&(m||o.exitDuration)&&this._ngZone.runOutsideAngular(()=>{let oe=()=>{R&&(R.fallbackTimer=null),clearTimeout(x),this._finishRippleTransition(E)},j=()=>this._destroyRipple(E),x=setTimeout(j,m+100);u.addEventListener(`transitionend`,oe),u.addEventListener(`transitioncancel`,j),R={onTransitionEnd:oe,onTransitionCancel:j,fallbackTimer:x}}),this._activeRipples.set(E,R),(y||!m)&&this._finishRippleTransition(E),E}fadeOutRipple(n){if(n.state===Ln.FADING_OUT||n.state===Ln.HIDDEN)return;let e=n.element,i=r(r({},Ec),n.config.animation);e.style.transitionDuration=`${i.exitDuration}ms`,e.style.opacity=`0`,n.state=Ln.FADING_OUT,(n._animationForciblyDisabledThroughCss||!i.exitDuration)&&this._finishRippleTransition(n)}fadeOutAll(){this._getActiveRipples().forEach(n=>n.fadeOut())}fadeOutAllNonPersistent(){this._getActiveRipples().forEach(n=>{n.config.persistent||n.fadeOut()})}setupTriggerEvents(n){let e=sn(n);!this._platform.isBrowser||!e||e===this._triggerElement||(this._removeTriggerEvents(),this._triggerElement=e,YC.forEach(i=>{t._eventManager.addHandler(this._ngZone,i,e,this)}))}handleEvent(n){n.type===`mousedown`?this._onMousedown(n):n.type===`touchstart`?this._onTouchStart(n):this._onPointerUp(),this._pointerUpEventsRegistered||(this._ngZone.runOutsideAngular(()=>{KC.forEach(e=>{this._triggerElement.addEventListener(e,this,qC)})}),this._pointerUpEventsRegistered=!0)}_finishRippleTransition(n){n.state===Ln.FADING_IN?this._startFadeOutTransition(n):n.state===Ln.FADING_OUT&&this._destroyRipple(n)}_startFadeOutTransition(n){let e=n===this._mostRecentTransientRipple,{persistent:i}=n.config;n.state=Ln.VISIBLE,!i&&(!e||!this._isPointerDown)&&n.fadeOut()}_destroyRipple(n){let e=this._activeRipples.get(n)??null;this._activeRipples.delete(n),this._activeRipples.size||(this._containerRect=null),n===this._mostRecentTransientRipple&&(this._mostRecentTransientRipple=null),n.state=Ln.HIDDEN,e!==null&&(n.element.removeEventListener(`transitionend`,e.onTransitionEnd),n.element.removeEventListener(`transitioncancel`,e.onTransitionCancel),e.fallbackTimer!==null&&clearTimeout(e.fallbackTimer)),n.element.remove()}_onMousedown(n){let e=So(n),i=this._lastTouchStartEvent&&Date.now()<this._lastTouchStartEvent+hA;!this._target.rippleDisabled&&!e&&!i&&(this._isPointerDown=!0,this.fadeInRipple(n.clientX,n.clientY,this._target.rippleConfig))}_onTouchStart(n){if(!this._target.rippleDisabled&&!wo(n)){this._lastTouchStartEvent=Date.now(),this._isPointerDown=!0;let e=n.changedTouches;if(e)for(let i=0;i<e.length;i++)this.fadeInRipple(e[i].clientX,e[i].clientY,this._target.rippleConfig)}}_onPointerUp(){this._isPointerDown&&(this._isPointerDown=!1,this._getActiveRipples().forEach(n=>{let e=n.state===Ln.VISIBLE||n.config.terminateOnPointerUp&&n.state===Ln.FADING_IN;!n.config.persistent&&e&&n.fadeOut()}))}_getActiveRipples(){return Array.from(this._activeRipples.keys())}_removeTriggerEvents(){let n=this._triggerElement;n&&(YC.forEach(e=>t._eventManager.removeHandler(e,n,this)),this._pointerUpEventsRegistered&&(KC.forEach(e=>n.removeEventListener(e,this,qC)),this._pointerUpEventsRegistered=!1))}};function gA(t,n,e){let i=Math.max(Math.abs(t-e.left),Math.abs(t-e.right)),a=Math.max(Math.abs(n-e.top),Math.abs(n-e.bottom));return Math.sqrt(i*i+a*a)}var Vr=new D(`mat-ripple-global-options`);var ci=(()=>{class t{_elementRef=f(G);_animationsDisabled=ze();color;unbounded=!1;centered=!1;radius=0;animation;get disabled(){return this._disabled}set disabled(e){e&&this.fadeOutAllNonPersistent(),this._disabled=e,this._setupTriggerEventsIfEnabled()}_disabled=!1;get trigger(){return this._trigger||this._elementRef.nativeElement}set trigger(e){this._trigger=e,this._setupTriggerEventsIfEnabled()}_trigger;_rippleRenderer;_globalOptions;_isInitialized=!1;constructor(){let e=f(K),i=f(Te),a=f(Vr,{optional:!0}),o=f(re);this._globalOptions=a||{},this._rippleRenderer=new Dc(this,e,this._elementRef,i,o)}ngOnInit(){this._isInitialized=!0,this._setupTriggerEventsIfEnabled()}ngOnDestroy(){this._rippleRenderer._removeTriggerEvents()}fadeOutAll(){this._rippleRenderer.fadeOutAll()}fadeOutAllNonPersistent(){this._rippleRenderer.fadeOutAllNonPersistent()}get rippleConfig(){return{centered:this.centered,radius:this.radius,color:this.color,animation:r(r(r({},this._globalOptions.animation),this._animationsDisabled?{enterDuration:0,exitDuration:0}:{}),this.animation),terminateOnPointerUp:this._globalOptions.terminateOnPointerUp}}get rippleDisabled(){return this.disabled||!!this._globalOptions.disabled}_setupTriggerEventsIfEnabled(){!this.disabled&&this._isInitialized&&this._rippleRenderer.setupTriggerEvents(this.trigger)}launch(e,i=0,a){return typeof e==`number`?this._rippleRenderer.fadeInRipple(e,i,r(r({},this.rippleConfig),a)):this._rippleRenderer.fadeInRipple(0,0,r(r({},this.rippleConfig),e))}static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,selectors:[[``,`mat-ripple`,``],[``,`matRipple`,``]],hostAttrs:[1,`mat-ripple`],hostVars:2,hostBindings:function(i,a){i&2&&W(`mat-ripple-unbounded`,a.unbounded)},inputs:{color:[0,`matRippleColor`,`color`],unbounded:[0,`matRippleUnbounded`,`unbounded`],centered:[0,`matRippleCentered`,`centered`],radius:[0,`matRippleRadius`,`radius`],animation:[0,`matRippleAnimation`,`animation`],disabled:[0,`matRippleDisabled`,`disabled`],trigger:[0,`matRippleTrigger`,`trigger`]},exportAs:[`matRipple`]})}return t})();var bA={capture:!0};var vA=[`focus`,`mousedown`,`mouseenter`,`touchstart`];var jg=`mat-ripple-loader-uninitialized`;var zg=`mat-ripple-loader-class-name`;var QC=`mat-ripple-loader-centered`;var Nu=`mat-ripple-loader-disabled`;var Tu=(()=>{class t{_document=f(ce);_animationsDisabled=ze();_globalRippleOptions=f(Vr,{optional:!0});_platform=f(Te);_ngZone=f(K);_injector=f(re);_eventCleanups;_hosts=new Map;constructor(){let e=f(kt).createRenderer(null,null);this._eventCleanups=this._ngZone.runOutsideAngular(()=>vA.map(i=>e.listen(this._document,i,this._onInteraction,bA)))}ngOnDestroy(){let e=this._hosts.keys();for(let i of e)this.destroyRipple(i);this._eventCleanups.forEach(i=>i())}configureRipple(e,i){e.setAttribute(jg,this._globalRippleOptions?.namespace??``),(i.className||!e.hasAttribute(zg))&&e.setAttribute(zg,i.className||``),i.centered&&e.setAttribute(QC,``),i.disabled&&e.setAttribute(Nu,``)}setDisabled(e,i){let a=this._hosts.get(e);a?(a.target.rippleDisabled=i,!i&&!a.hasSetUpEvents&&(a.hasSetUpEvents=!0,a.renderer.setupTriggerEvents(e))):i?e.setAttribute(Nu,``):e.removeAttribute(Nu)}_onInteraction=e=>{let i=qt(e);if(i instanceof HTMLElement){let a=i.closest(`[${jg}="${this._globalRippleOptions?.namespace??``}"]`);a&&this._createRipple(a)}};_createRipple(e){if(!this._document||this._hosts.has(e))return;e.querySelector(`.mat-ripple`)?.remove();let i=this._document.createElement(`span`);i.classList.add(`mat-ripple`,e.getAttribute(zg)),e.append(i);let a=this._globalRippleOptions,o=this._animationsDisabled?0:a?.animation?.enterDuration??Ec.enterDuration,r=this._animationsDisabled?0:a?.animation?.exitDuration??Ec.exitDuration,s={rippleDisabled:this._animationsDisabled||a?.disabled||e.hasAttribute(Nu),rippleConfig:{centered:e.hasAttribute(QC),terminateOnPointerUp:a?.terminateOnPointerUp,animation:{enterDuration:o,exitDuration:r}}},c=new Dc(s,this._ngZone,i,this._platform,this._injector),m=!s.rippleDisabled;m&&c.setupTriggerEvents(e),this._hosts.set(e,{target:s,renderer:c,hasSetUpEvents:m}),e.removeAttribute(jg)}destroyRipple(e){let i=this._hosts.get(e);i&&(i.renderer._removeTriggerEvents(),this._hosts.delete(e))}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var cn=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=B({type:t,selectors:[[`structural-styles`]],decls:0,vars:0,template:function(i,a){},styles:[`.mat-focus-indicator {
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
`],encapsulation:2})}return t})();var yA=new D(`MAT_BUTTON_CONFIG`);function XC(t){return t==null?void 0:st(t)}var Hg=(()=>{class t{_elementRef=f(G);_ngZone=f(K);_animationsDisabled=ze();_config=f(yA,{optional:!0});_focusMonitor=f(Xt);_cleanupClick;_renderer=f(Ae);_rippleLoader=f(Tu);_isAnchor;_isFab=!1;color;get disableRipple(){return this._disableRipple}set disableRipple(e){this._disableRipple=e,this._updateRippleDisabled()}_disableRipple=!1;get disabled(){return this._disabled}set disabled(e){this._disabled=e,this._updateRippleDisabled()}_disabled=!1;ariaDisabled;disabledInteractive;tabIndex;set _tabindex(e){this.tabIndex=e}showProgress=jt(!1,{transform:V});constructor(){f(Ze).load(cn);let e=this._elementRef.nativeElement;this._isAnchor=e.tagName===`A`,this.disabledInteractive=this._config?.disabledInteractive??!1,this.color=this._config?.color??null,this._rippleLoader?.configureRipple(e,{className:`mat-mdc-button-ripple`})}ngAfterViewInit(){this._focusMonitor.monitor(this._elementRef,!0),this._isAnchor&&this._setupAsAnchor()}ngOnDestroy(){this._cleanupClick?.(),this._focusMonitor.stopMonitoring(this._elementRef),this._rippleLoader?.destroyRipple(this._elementRef.nativeElement)}focus(e=`program`,i){e?this._focusMonitor.focusVia(this._elementRef.nativeElement,e,i):this._elementRef.nativeElement.focus(i)}_getAriaDisabled(){return this.ariaDisabled!=null?this.ariaDisabled:this._isAnchor?this.disabled||null:this.disabled&&this.disabledInteractive?!0:null}_getDisabledAttribute(){return this.disabledInteractive||!this.disabled?null:!0}_updateRippleDisabled(){this._rippleLoader?.setDisabled(this._elementRef.nativeElement,this.disableRipple||this.disabled)}_getTabIndex(){return this._isAnchor?this.disabled&&!this.disabledInteractive?-1:this.tabIndex:this.tabIndex}_setupAsAnchor(){this._cleanupClick=this._ngZone.runOutsideAngular(()=>this._renderer.listen(this._elementRef.nativeElement,`click`,e=>{this.disabled&&(e.preventDefault(),e.stopImmediatePropagation())}))}static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,hostAttrs:[1,`mat-mdc-button-base`],hostVars:15,hostBindings:function(i,a){i&2&&(ee(`disabled`,a._getDisabledAttribute())(`aria-disabled`,a._getAriaDisabled())(`tabindex`,a._getTabIndex()),et(a.color?`mat-`+a.color:``),W(`mat-mdc-button-progress-indicator-shown`,a.showProgress())(`mat-mdc-button-disabled`,a.disabled)(`mat-mdc-button-disabled-interactive`,a.disabledInteractive)(`mat-unthemed`,!a.color)(`_mat-animation-noopable`,a._animationsDisabled))},inputs:{color:`color`,disableRipple:[2,`disableRipple`,`disableRipple`,V],disabled:[2,`disabled`,`disabled`,V],ariaDisabled:[2,`aria-disabled`,`ariaDisabled`,V],disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,V],tabIndex:[2,`tabIndex`,`tabIndex`,XC],_tabindex:[2,`tabindex`,`_tabindex`,XC],showProgress:[1,`showProgress`]}})}return t})();var Ug=(()=>{class t extends Hg{constructor(){super(),this._rippleLoader.configureRipple(this._elementRef.nativeElement,{centered:!0})}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`,[[``,`progressIndicator`,``]]],i=[`*`,`[progressIndicator]`];function a(o,r){o&1&&(rt(0,`div`,1),ue(1,1),ut())}return B({type:t,selectors:[[`button`,`mat-icon-button`,``],[`a`,`mat-icon-button`,``],[`button`,`matIconButton`,``],[`a`,`matIconButton`,``]],hostAttrs:[1,`mdc-icon-button`,`mat-mdc-icon-button`],exportAs:[`matButton`,`matAnchor`],features:[Fe],ngContentSelectors:i,decls:5,vars:1,consts:[[1,`mat-mdc-button-persistent-ripple`,`mdc-icon-button__ripple`],[1,`mat-mdc-button-progress-indicator-container`],[1,`mat-focus-indicator`],[1,`mat-mdc-button-touch-target`]],template:function(r,s){r&1&&(Oe(e),Gt(0,`span`,0),ue(1),N(2,a,2,0,`div`,1),Gt(3,`span`,2)(4,`span`,3)),r&2&&(h(2),T(s.showProgress()?2:-1))},styles:[`.mat-mdc-icon-button {
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
`],encapsulation:2})})()}return t})();var _A=new D(`cdk-dir-doc`,{providedIn:`root`,factory:()=>f(ce)});var xA=/^(ar|ckb|dv|he|iw|fa|nqo|ps|sd|ug|ur|yi|.*[-_](Adlm|Arab|Hebr|Nkoo|Rohg|Thaa))(?!.*[-_](Latn|Cyrl)($|-|_))($|-|_)/i;function ZC(t){let n=t?.toLowerCase()||``;return n===`auto`&&typeof navigator<`u`&&navigator?.language?xA.test(navigator.language)?`rtl`:`ltr`:n===`rtl`?`rtl`:`ltr`}var pt=(()=>{class t{get value(){return this.valueSignal()}valueSignal=M(`ltr`);change=new J;constructor(){let e=f(_A,{optional:!0});if(e){let i=e.body?e.body.dir:null,a=e.documentElement?e.documentElement.dir:null;this.valueSignal.set(ZC(i||a||`ltr`))}}ngOnDestroy(){this.change.complete()}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var we=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({})}return t})();var ki=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({imports:[we]})}return t})();var JC=new Map([[`text`,[`mat-mdc-button`]],[`filled`,[`mdc-button--unelevated`,`mat-mdc-unelevated-button`]],[`elevated`,[`mdc-button--raised`,`mat-mdc-raised-button`]],[`outlined`,[`mdc-button--outlined`,`mat-mdc-outlined-button`]],[`tonal`,[`mat-tonal-button`]]]);var Zt=(()=>{class t extends Hg{get appearance(){return this._appearance}set appearance(e){this.setAppearance(e||this._config?.defaultAppearance||`text`)}_appearance=null;constructor(){super();let e=CA(this._elementRef.nativeElement);e&&this.setAppearance(e)}setAppearance(e){if(e===this._appearance)return;let i=this._elementRef.nativeElement.classList,a=this._appearance?JC.get(this._appearance):null,o=JC.get(e);a&&i.remove(...a),i.add(...o),this._appearance=e}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[[[``,8,`material-icons`,3,`iconPositionEnd`,``],[`mat-icon`,3,`iconPositionEnd`,``],[``,`matButtonIcon`,``,3,`iconPositionEnd`,``],[``,8,`material-symbols-outlined`,3,`iconPositionEnd`,``],[``,8,`material-symbols-rounded`,3,`iconPositionEnd`,``],[``,8,`material-symbols-sharp`,3,`iconPositionEnd`,``]],`*`,[[``,`iconPositionEnd`,``,8,`material-icons`],[`mat-icon`,`iconPositionEnd`,``],[``,`matButtonIcon`,``,`iconPositionEnd`,``],[``,`iconPositionEnd`,``,8,`material-symbols-outlined`],[``,`iconPositionEnd`,``,8,`material-symbols-rounded`],[``,`iconPositionEnd`,``,8,`material-symbols-sharp`]],[[``,`progressIndicator`,``]]],i=[`.material-icons:not([iconPositionEnd]), mat-icon:not([iconPositionEnd]), [matButtonIcon]:not([iconPositionEnd]), .material-symbols-outlined:not([iconPositionEnd]), .material-symbols-rounded:not([iconPositionEnd]), .material-symbols-sharp:not([iconPositionEnd])`,`*`,`.material-icons[iconPositionEnd], mat-icon[iconPositionEnd], [matButtonIcon][iconPositionEnd], .material-symbols-outlined[iconPositionEnd], .material-symbols-rounded[iconPositionEnd], .material-symbols-sharp[iconPositionEnd]`,`[progressIndicator]`];function a(o,r){o&1&&(rt(0,`div`,2),ue(1,3),ut())}return B({type:t,selectors:[[`button`,`matButton`,``],[`a`,`matButton`,``],[`button`,`mat-button`,``],[`button`,`mat-raised-button`,``],[`button`,`mat-flat-button`,``],[`button`,`mat-stroked-button`,``],[`a`,`mat-button`,``],[`a`,`mat-raised-button`,``],[`a`,`mat-flat-button`,``],[`a`,`mat-stroked-button`,``]],hostAttrs:[1,`mdc-button`],inputs:{appearance:[0,`matButton`,`appearance`]},exportAs:[`matButton`,`matAnchor`],features:[Fe],ngContentSelectors:i,decls:8,vars:5,consts:[[1,`mat-mdc-button-persistent-ripple`],[1,`mdc-button__label`],[1,`mat-mdc-button-progress-indicator-container`],[1,`mat-focus-indicator`],[1,`mat-mdc-button-touch-target`]],template:function(r,s){r&1&&(Oe(e),Gt(0,`span`,0),ue(1),rt(2,`span`,1),ue(3,1),ut(),ue(4,2),N(5,a,2,0,`div`,2),Gt(6,`span`,3)(7,`span`,4)),r&2&&(W(`mdc-button__ripple`,!s._isFab)(`mdc-fab__ripple`,s._isFab),h(5),T(s.showProgress()?5:-1))},styles:[`.mat-mdc-button-base {
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
`],encapsulation:2})})()}return t})();function CA(t){return t.hasAttribute(`mat-raised-button`)?`elevated`:t.hasAttribute(`mat-stroked-button`)?`outlined`:t.hasAttribute(`mat-flat-button`)?`filled`:t.hasAttribute(`mat-button`)?`text`:null}var Tt=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({imports:[ki,we]})}return t})();var SA=new D(`MAT_CARD_CONFIG`);var Vn=(()=>{class t{appearance;constructor(){let e=f(SA,{optional:!0});this.appearance=e?.appearance||`raised`}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){return B({type:t,selectors:[[`mat-card`]],hostAttrs:[1,`mat-mdc-card`,`mdc-card`],hostVars:8,hostBindings:function(a,o){a&2&&W(`mat-mdc-card-outlined`,o.appearance===`outlined`)(`mdc-card--outlined`,o.appearance===`outlined`)(`mat-mdc-card-filled`,o.appearance===`filled`)(`mdc-card--filled`,o.appearance===`filled`)},inputs:{appearance:`appearance`},exportAs:[`matCard`],ngContentSelectors:[`*`],decls:1,vars:0,template:function(a,o){a&1&&(Oe(),ue(0))},styles:[`.mat-mdc-card {
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
`],encapsulation:2})})()}return t})();var ka=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,selectors:[[`mat-card-title`],[``,`mat-card-title`,``],[``,`matCardTitle`,``]],hostAttrs:[1,`mat-mdc-card-title`]})}return t})();var Bn=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,selectors:[[`mat-card-content`]],hostAttrs:[1,`mat-mdc-card-content`]})}return t})();var Au=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,selectors:[[`mat-card-subtitle`],[``,`mat-card-subtitle`,``],[``,`matCardSubtitle`,``]],hostAttrs:[1,`mat-mdc-card-subtitle`]})}return t})();var Ia=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[[[``,`mat-card-avatar`,``],[``,`matCardAvatar`,``]],[[`mat-card-title`],[`mat-card-subtitle`],[``,`mat-card-title`,``],[``,`mat-card-subtitle`,``],[``,`matCardTitle`,``],[``,`matCardSubtitle`,``]],`*`];return B({type:t,selectors:[[`mat-card-header`]],hostAttrs:[1,`mat-mdc-card-header`],ngContentSelectors:[`[mat-card-avatar], [matCardAvatar]`,`mat-card-title, mat-card-subtitle,
      [mat-card-title], [mat-card-subtitle],
      [matCardTitle], [matCardSubtitle]`,`*`],decls:4,vars:0,consts:[[1,`mat-mdc-card-header-text`]],template:function(o,r){o&1&&(Oe(e),ue(0),rt(1,`div`,0),ue(2,1),ut(),ue(3,2))},encapsulation:2})})()}return t})();var jn=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({imports:[we]})}return t})();var wA=new D(`mat-progress-spinner-default-options`,{providedIn:`root`,factory:()=>({diameter:eS})});var eS=100;var EA=10;var tS=(()=>{class t{_elementRef=f(G);_noopAnimations;get color(){return this._color||this._defaultColor}set color(e){this._color=e}_color;_defaultColor=`primary`;_determinateCircle;constructor(){let e=f(wA),i=wc(),a=this._elementRef.nativeElement;this._noopAnimations=i===`di-disabled`&&!!e&&!e._forceAnimations,this.mode=a.nodeName.toLowerCase()===`mat-spinner`?`indeterminate`:`determinate`,!this._noopAnimations&&i===`reduced-motion`&&a.classList.add(`mat-progress-spinner-reduced-motion`),e&&(e.color&&(this.color=this._defaultColor=e.color),e.diameter&&(this.diameter=e.diameter),e.strokeWidth&&(this.strokeWidth=e.strokeWidth))}mode;get value(){return this.mode===`determinate`?this._value:0}set value(e){this._value=Math.max(0,Math.min(100,e||0))}_value=0;get diameter(){return this._diameter}set diameter(e){this._diameter=e||0}_diameter=eS;get strokeWidth(){return this._strokeWidth??this.diameter/10}set strokeWidth(e){this._strokeWidth=e||0}_strokeWidth;_circleRadius(){return(this.diameter-EA)/2}_viewBox(){let e=this._circleRadius()*2+this.strokeWidth;return`0 0 ${e} ${e}`}_strokeCircumference(){return 2*Math.PI*this._circleRadius()}_strokeDashOffset(){return this.mode===`determinate`?this._strokeCircumference()*(100-this._value)/100:null}_circleStrokeWidth(){return this.strokeWidth/this.diameter*100}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`determinateSpinner`];function i(a,o){if(a&1&&($t(),l(0,`svg`,11),z(1,`circle`,12),d()),a&2){let r=_();ee(`viewBox`,r._viewBox()),h(),mn(`stroke-dasharray`,r._strokeCircumference(),`px`)(`stroke-dashoffset`,r._strokeCircumference()/2,`px`)(`stroke-width`,r._circleStrokeWidth(),`%`),ee(`r`,r._circleRadius())}}return B({type:t,selectors:[[`mat-progress-spinner`],[`mat-spinner`]],viewQuery:function(o,r){if(o&1&&Xe(e,5),o&2){let s;X(s=Z())&&(r._determinateCircle=s.first)}},hostAttrs:[`role`,`progressbar`,`tabindex`,`-1`,1,`mat-mdc-progress-spinner`,`mdc-circular-progress`],hostVars:18,hostBindings:function(o,r){o&2&&(ee(`aria-valuemin`,0)(`aria-valuemax`,100)(`aria-valuenow`,r.mode===`determinate`?r.value:null)(`mode`,r.mode),et(`mat-`+r.color),mn(`width`,r.diameter,`px`)(`height`,r.diameter,`px`)(`--%NS%mat-progress-spinner-size`,r.diameter+`px`)(`--%NS%mat-progress-spinner-active-indicator-width`,r.diameter+`px`),W(`_mat-animation-noopable`,r._noopAnimations)(`mdc-circular-progress--indeterminate`,r.mode===`indeterminate`))},inputs:{color:`color`,mode:`mode`,value:[2,`value`,`value`,st],diameter:[2,`diameter`,`diameter`,st],strokeWidth:[2,`strokeWidth`,`strokeWidth`,st]},exportAs:[`matProgressSpinner`],decls:14,vars:11,consts:[[`circle`,``],[`determinateSpinner`,``],[`aria-hidden`,`true`,1,`mdc-circular-progress__determinate-container`],[`xmlns`,`http://www.w3.org/2000/svg`,`focusable`,`false`,1,`mdc-circular-progress__determinate-circle-graphic`],[`cx`,`50%`,`cy`,`50%`,1,`mdc-circular-progress__determinate-circle`],[`aria-hidden`,`true`,1,`mdc-circular-progress__indeterminate-container`],[1,`mdc-circular-progress__spinner-layer`],[1,`mdc-circular-progress__circle-clipper`,`mdc-circular-progress__circle-left`],[3,`ngTemplateOutlet`],[1,`mdc-circular-progress__gap-patch`],[1,`mdc-circular-progress__circle-clipper`,`mdc-circular-progress__circle-right`],[`xmlns`,`http://www.w3.org/2000/svg`,`focusable`,`false`,1,`mdc-circular-progress__indeterminate-circle-graphic`],[`cx`,`50%`,`cy`,`50%`]],template:function(o,r){if(o&1&&(at(0,i,2,8,`ng-template`,null,0,ac),l(2,`div`,2,1),$t(),l(4,`svg`,3),z(5,`circle`,4),d()(),va(),l(6,`div`,5)(7,`div`,6)(8,`div`,7),Sa(9,8),d(),l(10,`div`,9),Sa(11,8),d(),l(12,`div`,10),Sa(13,8),d()()()),o&2){let s=mt(1);h(4),ee(`viewBox`,r._viewBox()),h(),mn(`stroke-dasharray`,r._strokeCircumference(),`px`)(`stroke-dashoffset`,r._strokeDashOffset(),`px`)(`stroke-width`,r._circleStrokeWidth(),`%`),ee(`r`,r._circleRadius()),h(4),S(`ngTemplateOutlet`,s),h(2),S(`ngTemplateOutlet`,s),h(2),S(`ngTemplateOutlet`,s)}},dependencies:[pc],styles:[`.mat-mdc-progress-spinner {
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
`],encapsulation:2})})()}return t})();var nS=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({imports:[we]})}return t})();var uS=(()=>{class t{_renderer;_elementRef;onChange=e=>{};onTouched=()=>{};constructor(e,i){this._renderer=e,this._elementRef=i}setProperty(e,i){this._renderer.setProperty(this._elementRef.nativeElement,e,i)}registerOnTouched(e){this.onTouched=e}registerOnChange(e){this.onChange=e}setDisabledState(e){this.setProperty(`disabled`,e)}static ɵfac=function(i){return new(i||t)(je(Ae),je(G))};static ɵdir=q({type:t})}return t})();var kA=(()=>{class t extends uS{static ɵfac=(()=>{let e;return function(a){return(e||(e=It(t)))(a||t)}})();static ɵdir=q({type:t,features:[Fe]})}return t})();var Ur=new D(``);var IA={provide:Ur,useExisting:zt(()=>Uu),multi:!0};function MA(){let t=Pn()?Pn().getUserAgent():``;return/android (\d+)/.test(t.toLowerCase())}var NA=new D(``);var Uu=(()=>{class t extends uS{_compositionMode;_composing=!1;constructor(e,i,a){super(e,i),this._compositionMode=a,this._compositionMode??=!MA()}writeValue(e){let i=e??``;this.setProperty(`value`,i)}_handleInput(e){(!this._compositionMode||this._compositionMode&&!this._composing)&&this.onChange(e)}_compositionStart(){this._composing=!0}_compositionEnd(e){this._composing=!1,this._compositionMode&&this.onChange(e)}static ɵfac=function(i){return new(i||t)(je(Ae),je(G),je(NA,8))};static ɵdir=q({type:t,selectors:[[`input`,`formControlName`,``,3,`type`,`checkbox`,3,`ngNoCva`,``],[`textarea`,`formControlName`,``,3,`ngNoCva`,``],[`input`,`formControl`,``,3,`type`,`checkbox`,3,`ngNoCva`,``],[`textarea`,`formControl`,``,3,`ngNoCva`,``],[`input`,`ngModel`,``,3,`type`,`checkbox`,3,`ngNoCva`,``],[`textarea`,`ngModel`,``,3,`ngNoCva`,``],[``,`ngDefaultControl`,``]],hostBindings:function(i,a){i&1&&C(`input`,function(r){return a._handleInput(r.target.value)})(`blur`,function(){return a.onTouched()})(`compositionstart`,function(){return a._compositionStart()})(`compositionend`,function(r){return a._compositionEnd(r.target.value)})},standalone:!1,features:[$e([IA]),Fe]})}return t})();function qg(t){return t==null||Yg(t)===0}function Yg(t){return t==null?null:Array.isArray(t)||typeof t==`string`?t.length:t instanceof Set?t.size:null}var $r=new D(``);var Kg=new D(``);var TA=/^(?=.{1,254}$)(?=.{1,64}@)[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;var Ao=class{static min(n){return AA(n)}static max(n){return OA(n)}static required(n){return mS(n)}static requiredTrue(n){return RA(n)}static email(n){return PA(n)}static minLength(n){return FA(n)}static maxLength(n){return LA(n)}static pattern(n){return VA(n)}static nullValidator(n){return Ru()}static compose(n){return vS(n)}static composeAsync(n){return yS(n)}};function AA(t){return n=>{if(n.value==null||t==null)return null;let e=parseFloat(n.value);return!isNaN(e)&&e<t?{min:{min:t,actual:n.value}}:null}}function OA(t){return n=>{if(n.value==null||t==null)return null;let e=parseFloat(n.value);return!isNaN(e)&&e>t?{max:{max:t,actual:n.value}}:null}}function mS(t){return qg(t.value)?{required:!0}:null}function RA(t){return t.value===!0?null:{required:!0}}function PA(t){return qg(t.value)||TA.test(t.value)?null:{email:!0}}function FA(t){return n=>{let e=n.value?.length??Yg(n.value);return e===null||e===0?null:e<t?{minlength:{requiredLength:t,actualLength:e}}:null}}function LA(t){return n=>{let e=n.value?.length??Yg(n.value);return e!==null&&e>t?{maxlength:{requiredLength:t,actualLength:e}}:null}}function VA(t){if(!t)return Ru;let n,e;return typeof t==`string`?(e=``,t.charAt(0)!==`^`&&(e+=`^`),e+=t,t.charAt(t.length-1)!==`$`&&(e+=`$`),n=new RegExp(e)):(e=t.toString(),n=t),i=>{if(qg(i.value))return null;let a=i.value;return n.test(a)?null:{pattern:{requiredPattern:e,actualValue:a}}}}function Ru(t){return null}function pS(t){return t!=null}function hS(t){return _o(t)?kn(t):t}function fS(t){let n={};return t.forEach(e=>{n=e!=null?r(r({},n),e):n}),Object.keys(n).length===0?null:n}function gS(t,n){return n.map(e=>e(t))}function BA(t){return!t.validate}function bS(t){return t.map(n=>BA(n)?n:e=>n.validate(e))}function vS(t){if(!t)return null;let n=t.filter(pS);return n.length==0?null:function(e){return fS(gS(e,n))}}function Qg(t){return t!=null?vS(bS(t)):null}function yS(t){if(!t)return null;let n=t.filter(pS);return n.length==0?null:function(e){return tp(gS(e,n).map(hS)).pipe(ot(fS))}}function Xg(t){return t!=null?yS(bS(t)):null}function iS(t,n){return t===null?[n]:Array.isArray(t)?[...t,n]:[t,n]}function _S(t){return t._rawValidators}function xS(t){return t._rawAsyncValidators}function $g(t){return t?Array.isArray(t)?t:[t]:[]}function Pu(t,n){return Array.isArray(t)?t.includes(n):t===n}function aS(t,n){let e=$g(n);return $g(t).forEach(a=>{Pu(e,a)||e.push(a)}),e}function oS(t,n){return $g(n).filter(e=>!Pu(t,e))}var Fu=class{get value(){return this.control?this.control.value:null}get valid(){return this.control?this.control.valid:null}get invalid(){return this.control?this.control.invalid:null}get pending(){return this.control?this.control.pending:null}get disabled(){return this.control?this.control.disabled:null}get enabled(){return this.control?this.control.enabled:null}get errors(){return this.control?this.control.errors:null}get pristine(){return this.control?this.control.pristine:null}get dirty(){return this.control?this.control.dirty:null}get touched(){return this.control?this.control.touched:null}get status(){return this.control?this.control.status:null}get untouched(){return this.control?this.control.untouched:null}get statusChanges(){return this.control?this.control.statusChanges:null}get valueChanges(){return this.control?this.control.valueChanges:null}get path(){return null}_composedValidatorFn;_composedAsyncValidatorFn;_rawValidators=[];_rawAsyncValidators=[];_setValidators(n){this._rawValidators=n||[],this._composedValidatorFn=Qg(this._rawValidators)}_setAsyncValidators(n){this._rawAsyncValidators=n||[],this._composedAsyncValidatorFn=Xg(this._rawAsyncValidators)}get validator(){return this._composedValidatorFn||null}get asyncValidator(){return this._composedAsyncValidatorFn||null}_onDestroyCallbacks=[];_registerOnDestroy(n){this._onDestroyCallbacks.push(n)}_invokeOnDestroyCallbacks(){this._onDestroyCallbacks.forEach(n=>n()),this._onDestroyCallbacks=[]}reset(n=void 0){this.control?.reset(n)}hasError(n,e){return this.control?this.control.hasError(n,e):!1}getError(n,e){return this.control?this.control.getError(n,e):null}};var Oo=class extends Fu{name;get formDirective(){return null}get path(){return null}};var kc=`VALID`;var Ou=`INVALID`;var jr=`PENDING`;var Ic=`DISABLED`;var Ma=class{};var Lu=class extends Ma{value;source;constructor(n,e){super(),this.value=n,this.source=e}};var Nc=class extends Ma{pristine;source;constructor(n,e){super(),this.pristine=n,this.source=e}};var Tc=class extends Ma{touched;source;constructor(n,e){super(),this.touched=n,this.source=e}};var zr=class extends Ma{status;source;constructor(n,e){super(),this.status=n,this.source=e}};var Vu=class extends Ma{source;constructor(n){super(),this.source=n}};var Hr=class extends Ma{source;constructor(n){super(),this.source=n}};function CS(t){return($u(t)?t.validators:t)||null}function jA(t){return Array.isArray(t)?Qg(t):t||null}function SS(t,n){return($u(n)?n.asyncValidators:t)||null}function zA(t){return Array.isArray(t)?Xg(t):t||null}function $u(t){return t!=null&&!Array.isArray(t)&&typeof t==`object`}function HA(t,n,e){let i=t.controls;if(!(n?Object.keys(i):i).length)throw new $(1e3,``);if(!wS(i,e))throw new $(1001,``)}function UA(t,n,e){t._forEachChild((i,a)=>{if(e[a]===void 0)throw new $(-1002,``)})}var Bu=class{_pendingDirty=!1;_hasOwnPendingAsyncValidator=null;_pendingTouched=!1;_onCollectionChange=()=>{};_updateOn;_hasRequired=M(!1);_parent=null;_asyncValidationSubscription;_composedValidatorFn;_composedAsyncValidatorFn;_rawValidators;_rawAsyncValidators;value;constructor(n,e){this._assignValidators(n),this._assignAsyncValidators(e)}get validator(){return this._composedValidatorFn}set validator(n){this._rawValidators=this._composedValidatorFn=n,this._updateHasRequiredValidator()}get asyncValidator(){return this._composedAsyncValidatorFn}set asyncValidator(n){this._rawAsyncValidators=this._composedAsyncValidatorFn=n}get parent(){return this._parent}get status(){return tt(this.statusReactive)}set status(n){tt(()=>this.statusReactive.set(n))}_status=ne(()=>this.statusReactive());statusReactive=M(void 0);get valid(){return this.status===kc}get invalid(){return this.status===Ou}get pending(){return this.status===jr}get disabled(){return this.status===Ic}get enabled(){return this.status!==Ic}errors;get pristine(){return tt(this.pristineReactive)}set pristine(n){tt(()=>this.pristineReactive.set(n))}_pristine=ne(()=>this.pristineReactive());pristineReactive=M(!0);get dirty(){return!this.pristine}get touched(){return tt(this.touchedReactive)}set touched(n){tt(()=>this.touchedReactive.set(n))}_touched=ne(()=>this.touchedReactive());touchedReactive=M(!1);get untouched(){return!this.touched}_events=new L;events=this._events.asObservable();valueChanges;statusChanges;get updateOn(){return this._updateOn?this._updateOn:this.parent?this.parent.updateOn:`change`}setValidators(n){this._assignValidators(n)}setAsyncValidators(n){this._assignAsyncValidators(n)}addValidators(n){this.setValidators(aS(n,this._rawValidators))}addAsyncValidators(n){this.setAsyncValidators(aS(n,this._rawAsyncValidators))}removeValidators(n){this.setValidators(oS(n,this._rawValidators))}removeAsyncValidators(n){this.setAsyncValidators(oS(n,this._rawAsyncValidators))}hasValidator(n){return Pu(this._rawValidators,n)}hasAsyncValidator(n){return Pu(this._rawAsyncValidators,n)}clearValidators(){this.validator=null}clearAsyncValidators(){this.asyncValidator=null}markAsTouched(n={}){let e=this.touched===!1;this.touched=!0;let i=n.sourceControl??this;n.onlySelf||this._parent?.markAsTouched(s(r({},n),{sourceControl:i})),e&&n.emitEvent!==!1&&this._events.next(new Tc(!0,i))}markAllAsDirty(n={}){this.markAsDirty({onlySelf:!0,emitEvent:n.emitEvent,sourceControl:this}),this._forEachChild(e=>e.markAllAsDirty(n))}markAllAsTouched(n={}){this.markAsTouched({onlySelf:!0,emitEvent:n.emitEvent,sourceControl:this}),this._forEachChild(e=>e.markAllAsTouched(n))}markAsUntouched(n={}){let e=this.touched===!0;this.touched=!1,this._pendingTouched=!1;let i=n.sourceControl??this;this._forEachChild(a=>{a.markAsUntouched({onlySelf:!0,emitEvent:n.emitEvent,sourceControl:i})}),n.onlySelf||this._parent?._updateTouched(n,i),e&&n.emitEvent!==!1&&this._events.next(new Tc(!1,i))}markAsDirty(n={}){let e=this.pristine===!0;this.pristine=!1;let i=n.sourceControl??this;n.onlySelf||this._parent?.markAsDirty(s(r({},n),{sourceControl:i})),e&&n.emitEvent!==!1&&this._events.next(new Nc(!1,i))}markAsPristine(n={}){let e=this.pristine===!1;this.pristine=!0,this._pendingDirty=!1;let i=n.sourceControl??this;this._forEachChild(a=>{a.markAsPristine({onlySelf:!0,emitEvent:n.emitEvent})}),n.onlySelf||this._parent?._updatePristine(n,i),e&&n.emitEvent!==!1&&this._events.next(new Nc(!0,i))}markAsPending(n={}){this.status=jr;let e=n.sourceControl??this;n.emitEvent!==!1&&(this._events.next(new zr(this.status,e)),this.statusChanges.emit(this.status)),n.onlySelf||this._parent?.markAsPending(s(r({},n),{sourceControl:e}))}disable(n={}){let e=this._parentMarkedDirty(n.onlySelf);this.status=Ic,this.errors=null,this._forEachChild(a=>{a.disable(s(r({},n),{onlySelf:!0}))}),this._updateValue();let i=n.sourceControl??this;n.emitEvent!==!1&&(this._events.next(new Lu(this.value,i)),this._events.next(new zr(this.status,i)),this.valueChanges.emit(this.value),this.statusChanges.emit(this.status)),this._updateAncestors(s(r({},n),{skipPristineCheck:e}),this),this._onDisabledChange.forEach(a=>a(!0))}enable(n={}){let e=this._parentMarkedDirty(n.onlySelf);this.status=kc,this._forEachChild(i=>{i.enable(s(r({},n),{onlySelf:!0}))}),this.updateValueAndValidity({onlySelf:!0,emitEvent:n.emitEvent}),this._updateAncestors(s(r({},n),{skipPristineCheck:e}),this),this._onDisabledChange.forEach(i=>i(!1))}_updateAncestors(n,e){n.onlySelf||(this._parent?.updateValueAndValidity(n),n.skipPristineCheck||this._parent?._updatePristine({},e),this._parent?._updateTouched({},e))}setParent(n){this._parent=n}getRawValue(){return this.value}updateValueAndValidity(n={}){if(this._setInitialStatus(),this._updateValue(),this.enabled){let i=this._cancelExistingSubscription();this.errors=this._runValidator(),this.status=this._calculateStatus(),(this.status===kc||this.status===jr)&&this._runAsyncValidator(i,n.emitEvent)}let e=n.sourceControl??this;n.emitEvent!==!1&&(this._events.next(new Lu(this.value,e)),this._events.next(new zr(this.status,e)),this.valueChanges.emit(this.value),this.statusChanges.emit(this.status)),n.onlySelf||this._parent?.updateValueAndValidity(s(r({},n),{sourceControl:e}))}_updateTreeValidity(n={emitEvent:!0}){this._forEachChild(e=>e._updateTreeValidity(n)),this.updateValueAndValidity({onlySelf:!0,emitEvent:n.emitEvent})}_setInitialStatus(){this.status=this._allControlsDisabled()?Ic:kc}_runValidator(){return this.validator?this.validator(this):null}_runAsyncValidator(n,e){if(this.asyncValidator){this.status=jr,this._hasOwnPendingAsyncValidator={emitEvent:e!==!1,shouldHaveEmitted:n!==!1};let i=hS(this.asyncValidator(this));this._asyncValidationSubscription=i.subscribe(a=>{this._hasOwnPendingAsyncValidator=null,this.setErrors(a,{emitEvent:e,shouldHaveEmitted:n})})}}_cancelExistingSubscription(){if(this._asyncValidationSubscription){this._asyncValidationSubscription.unsubscribe();let n=(this._hasOwnPendingAsyncValidator?.emitEvent||this._hasOwnPendingAsyncValidator?.shouldHaveEmitted)??!1;return this._hasOwnPendingAsyncValidator=null,n}return!1}setErrors(n,e={}){this.errors=n,this._updateControlsErrors(e.emitEvent!==!1,this,e.shouldHaveEmitted)}get(n){let e=n;return e==null||(Array.isArray(e)||(e=e.split(`.`)),e.length===0)?null:e.reduce((i,a)=>i&&i._find(a),this)}getError(n,e){let i=e?this.get(e):this;return i?.errors?i.errors[n]:null}hasError(n,e){return!!this.getError(n,e)}get root(){let n=this;for(;n._parent;)n=n._parent;return n}_updateControlsErrors(n,e,i){this.status=this._calculateStatus(),n&&this.statusChanges.emit(this.status),(n||i)&&this._events.next(new zr(this.status,e)),this._parent&&this._parent._updateControlsErrors(n,e,i)}_initObservables(){this.valueChanges=new J,this.statusChanges=new J}_calculateStatus(){return this._allControlsDisabled()?Ic:this.errors?Ou:this._hasOwnPendingAsyncValidator||this._anyControlsHaveStatus(jr)?jr:this._anyControlsHaveStatus(Ou)?Ou:kc}_anyControlsHaveStatus(n){return this._anyControls(e=>e.status===n)}_anyControlsDirty(){return this._anyControls(n=>n.dirty)}_anyControlsTouched(){return this._anyControls(n=>n.touched)}_updatePristine(n,e){let i=!this._anyControlsDirty(),a=this.pristine!==i;this.pristine=i,n.onlySelf||this._parent?._updatePristine(n,e),a&&this._events.next(new Nc(this.pristine,e))}_updateTouched(n={},e){this.touched=this._anyControlsTouched(),this._events.next(new Tc(this.touched,e)),n.onlySelf||this._parent?._updateTouched(n,e)}_onDisabledChange=[];_registerOnCollectionChange(n){this._onCollectionChange=n}_setUpdateStrategy(n){$u(n)&&n.updateOn!=null&&(this._updateOn=n.updateOn)}_parentMarkedDirty(n){return!n&&!!this._parent?.dirty&&!this._parent._anyControlsDirty()}_find(n){return null}_assignValidators(n){this._rawValidators=Array.isArray(n)?n.slice():n,this._composedValidatorFn=jA(this._rawValidators),this._updateHasRequiredValidator()}_assignAsyncValidators(n){this._rawAsyncValidators=Array.isArray(n)?n.slice():n,this._composedAsyncValidatorFn=zA(this._rawAsyncValidators)}_updateHasRequiredValidator(){tt(()=>this._hasRequired.set(this.hasValidator(Ao.required)))}};function wS(t,n){return Object.hasOwn(t,n)}function $A(t){return t.tagName===`INPUT`||t.tagName===`SELECT`||t.tagName===`TEXTAREA`}function GA(t,n,e,i){switch(e){case`name`:t.setAttribute(n,e,i);break;case`disabled`:case`readonly`:case`required`:i?t.setAttribute(n,e,``):t.removeAttribute(n,e);break;case`max`:case`min`:case`minLength`:case`maxLength`:i!==void 0?t.setAttribute(n,e,i.toString()):t.removeAttribute(n,e)}}var Gg=class{kind;context;control;message;constructor({kind:n,context:e,control:i}){this.kind=n,this.context=e,this.control=i}};var WA=(()=>{class t{_validator=Ru;_onChange;_enabled;ngOnChanges(e){if(this.inputName in e){let i=this.normalizeInput(e[this.inputName].currentValue);this._enabled=this.enabled(i),this._validator=this._enabled?this.createValidator(i):Ru,this._onChange?.()}}validate(e){return this._validator(e)}registerOnValidatorChange(e){this._onChange=e}enabled(e){return e!=null}static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,features:[ft]})}return t})();var qA={provide:$r,useExisting:zt(()=>ES),multi:!0};var ES=(()=>{class t extends WA{required;inputName=`required`;normalizeInput=V;createValidator=e=>mS;enabled(e){return e}static ɵfac=(()=>{let e;return function(a){return(e||(e=It(t)))(a||t)}})();static ɵdir=q({type:t,selectors:[[``,`required`,``,`formControlName`,``,3,`type`,`checkbox`],[``,`required`,``,`formControl`,``,3,`type`,`checkbox`],[``,`required`,``,`ngModel`,``,3,`type`,`checkbox`]],hostVars:1,hostBindings:function(i,a){i&2&&ee(`required`,a._enabled?``:null)},inputs:{required:`required`},standalone:!1,features:[$e([qA]),Fe]})}return t})();var YA=new D(``);var Gu=new D(``,{factory:()=>Zg});var Zg=`always`;function KA(t,n){return[...n.path,t]}function rS(t,n,e=Zg){Jg(t,n),n.valueAccessor.writeValue(t.value),(t.disabled||e===`always`)&&n.valueAccessor.setDisabledState?.(t.disabled),XA(t,n),JA(t,n),ZA(t,n),QA(t,n)}function sS(t,n,e=!0){let i=()=>{};n?.valueAccessor?.registerOnChange(i),n?.valueAccessor?.registerOnTouched(i),zu(t,n),t&&(n._invokeOnDestroyCallbacks(),t._registerOnCollectionChange(()=>{}))}function ju(t,n){t.forEach(e=>{e.registerOnValidatorChange&&e.registerOnValidatorChange(n)})}function QA(t,n){if(n.valueAccessor.setDisabledState){let e=i=>{n.valueAccessor.setDisabledState(i)};t.registerOnDisabledChange(e),n._registerOnDestroy(()=>{t._unregisterOnDisabledChange(e)})}}function Jg(t,n){let e=_S(t);n.validator!==null?t.setValidators(iS(e,n.validator)):typeof e==`function`&&t.setValidators([e]);let i=xS(t);n.asyncValidator!==null?t.setAsyncValidators(iS(i,n.asyncValidator)):typeof i==`function`&&t.setAsyncValidators([i]);let a=()=>t.updateValueAndValidity();ju(n._rawValidators,a),ju(n._rawAsyncValidators,a)}function zu(t,n){let e=!1;if(t!==null){if(n.validator!==null){let a=_S(t);if(Array.isArray(a)&&a.length>0){let o=a.filter(r=>r!==n.validator);o.length!==a.length&&(e=!0,t.setValidators(o))}}if(n.asyncValidator!==null){let a=xS(t);if(Array.isArray(a)&&a.length>0){let o=a.filter(r=>r!==n.asyncValidator);o.length!==a.length&&(e=!0,t.setAsyncValidators(o))}}}let i=()=>{};return ju(n._rawValidators,i),ju(n._rawAsyncValidators,i),e}function XA(t,n){n.valueAccessor.registerOnChange(e=>{t._pendingValue=e,t._pendingChange=!0,t._pendingDirty=!0,t.updateOn===`change`&&DS(t,n)})}function ZA(t,n){n.valueAccessor.registerOnTouched(()=>{t._pendingTouched=!0,t.updateOn===`blur`&&t._pendingChange&&DS(t,n),t.updateOn!==`submit`&&t.markAsTouched()})}function DS(t,n){t._pendingDirty&&t.markAsDirty(),t.setValue(t._pendingValue,{emitModelToViewChange:!1}),n.viewToModelUpdate(t._pendingValue),t._pendingChange=!1}function JA(t,n){let e=(i,a)=>{n.valueAccessor.writeValue(i),a&&n.viewToModelUpdate(i)};t.registerOnChange(e),n._registerOnDestroy(()=>{t._unregisterOnChange(e)})}function kS(t,n){Jg(t,n)}function eO(t,n){return zu(t,n)}function tO(t,n){if(!Object.hasOwn(t,`model`))return!1;let e=t.model;return e.isFirstChange()?!0:!Object.is(n,e.currentValue)}function nO(t){return Object.getPrototypeOf(t.constructor)===kA}function IS(t,n){t._syncPendingControls(),n.forEach(e=>{let i=e.control;i.updateOn===`submit`&&i._pendingChange&&(e.viewToModelUpdate(i._pendingValue),i._pendingChange=!1)})}function iO(t,n){if(!n)return null;let e,i,a;return n.forEach(o=>{o.constructor===Uu?e=o:nO(o)?i=o:a=o}),a||i||e||null}function aO(t,n){let e=t.indexOf(n);e>-1&&t.splice(e,1)}var oO={provide:YA,useFactory:()=>{let t=f(Ii,{self:!0});return{setParseErrors:n=>{t.setParseErrorSource(n)},set onReset(n){t.onReset=n}}}};var Ii=class extends Fu{_parent=null;name=null;valueAccessor=null;isCustomControlBased=!1;userOnReset;resetSubscription;set onReset(n){this.userOnReset=n,this.resetSubscription?.unsubscribe(),this.resetSubscription=void 0,this.control&&(this.resetSubscription=this.control.events.subscribe(e=>{e instanceof Hr&&this.control&&this.userOnReset?.(this.control.value)}),this.subscription?.add(this.resetSubscription))}isNativeFormElement=!1;rawValueAccessors;_selectedValueAccessor=null;get selectedValueAccessor(){return this._selectedValueAccessor??=iO(this,this.rawValueAccessors)}parseErrorsValidator=null;renderer;injector;requiredValidatorViaDi;subscription;customControlBindings=null;constructor(n,e,i){super(),this.injector=n,this.renderer=e,this.rawValueAccessors=i,this.injector?.get(Ne)?.onDestroy(()=>{this.removeParseErrorsValidator(this.control),this.subscription?.unsubscribe()})}setupCustomControl(){this.subscription?.unsubscribe();let n=this.injector?.get(Re);if(!this.control||!n)return;let e=n.markForCheck.bind(n);this.subscription=new De,this.subscription.add(this.control.valueChanges.subscribe(e)),this.subscription.add(this.control.statusChanges.subscribe(e)),this.resetSubscription?.unsubscribe(),this.resetSubscription=void 0,this.userOnReset&&(this.resetSubscription=this.control.events.subscribe(i=>{i instanceof Hr&&this.control&&this.userOnReset?.(this.control.value)}),this.subscription.add(this.resetSubscription)),this.parseErrorsValidator&&this.control.addValidators(this.parseErrorsValidator)}ngControlCreate(n){!n.nativeElement.hasAttribute?.(`ngNoCva`)&&(this.rawValueAccessors&&this.rawValueAccessors.length>0||this.valueAccessor!==null)||!n.customControl||(this.isCustomControlBased=!0,n.listenToCustomControlModel(a=>{this.control?.markAsDirty(),this.control?.setValue(a,{emitModelToViewChange:!1}),this.viewToModelUpdate(a)}),n.listenToCustomControlOutput(`touch`,()=>{this.control?.markAsTouched()}),this.customControlBindings={},this.isNativeFormElement=$A(n.nativeElement),this.requiredValidatorViaDi=this._rawValidators.find(a=>a instanceof ES))}ngControlUpdate(n,e){if(!this.isCustomControlBased)return;let i=this.control,a=this.customControlBindings;Object.is(a.value,i.value)||(a.value=i.value,n.setCustomControlModelInput(i.value)),this.bindControlProperty(n,a,`touched`,i.touched),this.bindControlProperty(n,a,`dirty`,i.dirty),this.bindControlProperty(n,a,`valid`,i.valid),this.bindControlProperty(n,a,`invalid`,i.invalid),this.bindControlProperty(n,a,`pending`,i.pending),this.bindControlProperty(n,a,`disabled`,i.disabled),this.shouldBindRequired&&this.bindControlProperty(n,a,`required`,this.isRequired);let o=i.errors;if(a.errors!==o){a.errors=o;let r=this._convertErrors(o);n.setInputOnDirectives(`errors`,r)}}get isRequired(){return(this.requiredValidatorViaDi?._enabled||this.control?._hasRequired())??!1}get shouldBindRequired(){return!0}bindControlProperty(n,e,i,a){if(e[i]===a)return;e[i]=a;let o=n.setInputOnDirectives(i,a);this.isNativeFormElement&&!o&&(i===`disabled`||i===`required`)&&this.renderer&&GA(this.renderer,n.nativeElement,i,a)}_convertErrors(n){if(n===null)return[];let e=this.control;return Object.entries(n).map(([i,a])=>new Gg({context:a,kind:i,control:e}))}setParseErrorSource(n){if(n===void 0)return;let e=null,i=ne(()=>{let a=n();return a.length===0?null:a.reduce((o,r)=>(o[r.kind]=r,o),{})});this.parseErrorsValidator=(()=>e).bind(this),Pt(()=>{e=i(),this.control?.updateValueAndValidity({emitEvent:!1})},{injector:this.injector})}removeParseErrorsValidator(n){this.parseErrorsValidator&&(n?.removeValidators(this.parseErrorsValidator),n?.updateValueAndValidity({emitEvent:!1}))}};var Wg=class{_cd;constructor(n){this._cd=n}get isTouched(){return this._cd?.control?._touched?.(),!!this._cd?.control?.touched}get isUntouched(){return!!this._cd?.control?.untouched}get isPristine(){return this._cd?.control?._pristine?.(),!!this._cd?.control?.pristine}get isDirty(){return!!this._cd?.control?.dirty}get isValid(){return this._cd?.control?._status?.(),!!this._cd?.control?.valid}get isInvalid(){return!!this._cd?.control?.invalid}get isPending(){return!!this._cd?.control?.pending}get isSubmitted(){return this._cd?._submitted?.(),!!this._cd?.submitted}};var MS=(()=>{class t extends Wg{constructor(e){super(e)}static ɵfac=function(i){return new(i||t)(je(Ii,2))};static ɵdir=q({type:t,selectors:[[``,`formControlName`,``],[``,`ngModel`,``],[``,`formControl`,``]],hostVars:14,hostBindings:function(i,a){i&2&&W(`ng-untouched`,a.isUntouched)(`ng-touched`,a.isTouched)(`ng-pristine`,a.isPristine)(`ng-dirty`,a.isDirty)(`ng-valid`,a.isValid)(`ng-invalid`,a.isInvalid)(`ng-pending`,a.isPending)},standalone:!1,features:[Fe]})}return t})();var Hu=class extends Bu{constructor(n,e,i){super(CS(e),SS(i,e)),this.controls=n,this._initObservables(),this._setUpdateStrategy(e),this._setUpControls(),this.updateValueAndValidity({onlySelf:!0,emitEvent:!!this.asyncValidator})}controls;registerControl(n,e){return this._find(n)||(this.controls[n]=e,e.setParent(this),e._registerOnCollectionChange(this._onCollectionChange),e)}addControl(n,e,i={}){this.registerControl(n,e),this.updateValueAndValidity({emitEvent:i.emitEvent}),this._onCollectionChange()}removeControl(n,e={}){let i=this._find(n);i&&i._registerOnCollectionChange(()=>{}),delete this.controls[n],this.updateValueAndValidity({emitEvent:e.emitEvent}),this._onCollectionChange()}setControl(n,e,i={}){let a=this._find(n);a&&a._registerOnCollectionChange(()=>{}),delete this.controls[n],e&&this.registerControl(n,e),this.updateValueAndValidity({emitEvent:i.emitEvent}),this._onCollectionChange()}contains(n){return this._find(n)?.enabled===!0}setValue(n,e={}){tt(()=>{UA(this,!0,n),Object.keys(n).forEach(i=>{HA(this,!0,i),this.controls[i].setValue(n[i],{onlySelf:!0,emitEvent:e.emitEvent})}),this.updateValueAndValidity(e)})}patchValue(n,e={}){n!=null&&(Object.keys(n).forEach(i=>{let a=this._find(i);a&&a.patchValue(n[i],{onlySelf:!0,emitEvent:e.emitEvent})}),this.updateValueAndValidity(e))}reset(n={},e={}){this._forEachChild((i,a)=>{i.reset(n?n[a]:null,s(r({},e),{onlySelf:!0}))}),this._updatePristine(e,this),this._updateTouched(e,this),this.updateValueAndValidity(e),e?.emitEvent!==!1&&this._events.next(new Hr(this))}getRawValue(){return this._reduceChildren({},(n,e,i)=>(n[i]=e.getRawValue(),n))}_syncPendingControls(){let n=this._reduceChildren(!1,(e,i)=>i._syncPendingControls()?!0:e);return n&&this.updateValueAndValidity({onlySelf:!0}),n}_forEachChild(n){Object.keys(this.controls).forEach(e=>{let i=this.controls[e];i&&n(i,e)})}_setUpControls(){this._forEachChild(n=>{n.setParent(this),n._registerOnCollectionChange(this._onCollectionChange)})}_updateValue(){this.value=this._reduceValue()}_anyControls(n){for(let[e,i]of Object.entries(this.controls))if(this.contains(e)&&n(i))return!0;return!1}_reduceValue(){return this._reduceChildren({},(e,i,a)=>((i.enabled||this.disabled)&&(e[a]=i.value),e))}_reduceChildren(n,e){let i=n;return this._forEachChild((a,o)=>{i=e(i,a,o)}),i}_allControlsDisabled(){for(let n of Object.keys(this.controls))if(this.controls[n].enabled)return!1;return Object.keys(this.controls).length>0||this.disabled}_find(n){return wS(this.controls,n)?this.controls[n]:null}};var rO={provide:Oo,useExisting:zt(()=>Ac)};var Mc=Promise.resolve();var Ac=(()=>{class t extends Oo{callSetDisabledState;get submitted(){return tt(this.submittedReactive)}_submitted=ne(()=>this.submittedReactive());submittedReactive=M(!1);_directives=new Set;form;ngSubmit=new J;options;constructor(e,i,a){super(),this.callSetDisabledState=a,this.form=new Hu({},Qg(e),Xg(i))}ngAfterViewInit(){this._setUpdateStrategy()}get formDirective(){return this}get control(){return this.form}get path(){return[]}get controls(){return this.form.controls}addControl(e){Mc.then(()=>{e.control=this._findContainer(e.path).registerControl(e.name,e.control),e._setupWithForm(this.callSetDisabledState),e.control.updateValueAndValidity({emitEvent:!1}),this._directives.add(e)})}getControl(e){return this.form.get(e.path)}removeControl(e){Mc.then(()=>{this._findContainer(e.path)?.removeControl(e.name),this._directives.delete(e)})}addFormGroup(e){Mc.then(()=>{let i=this._findContainer(e.path),a=new Hu({});kS(a,e),i.registerControl(e.name,a),a.updateValueAndValidity({emitEvent:!1})})}removeFormGroup(e){Mc.then(()=>{this._findContainer(e.path)?.removeControl?.(e.name)})}getFormGroup(e){return this.form.get(e.path)}updateModel(e,i){Mc.then(()=>{this.form.get(e.path).setValue(i)})}setValue(e){this.control.setValue(e)}onSubmit(e){return this.submittedReactive.set(!0),IS(this.form,this._directives),this.ngSubmit.emit(e),this.form._events.next(new Vu(this.control)),e?.target?.method===`dialog`}onReset(){this.resetForm()}resetForm(e=void 0){this.form.reset(e),this.submittedReactive.set(!1)}_setUpdateStrategy(){this.options&&this.options.updateOn!=null&&(this.form._updateOn=this.options.updateOn)}_findContainer(e){return e.pop(),e.length?this.form.get(e):this.form}static ɵfac=function(i){return new(i||t)(je($r,10),je(Kg,10),je(Gu,8))};static ɵdir=q({type:t,selectors:[[`form`,3,`ngNoForm`,``,3,`formGroup`,``,3,`formArray`,``],[`ng-form`],[``,`ngForm`,``]],hostBindings:function(i,a){i&1&&C(`submit`,function(r){return a.onSubmit(r)})(`reset`,function(){return a.onReset()})},inputs:{options:[0,`ngFormOptions`,`options`]},outputs:{ngSubmit:`ngSubmit`},exportAs:[`ngForm`],standalone:!1,features:[$e([rO]),Fe]})}return t})();function cS(t,n){let e=t.indexOf(n);e>-1&&t.splice(e,1)}function lS(t){return typeof t==`object`&&t!==null&&Object.keys(t).length===2&&`value`in t&&`disabled`in t}var NS=class extends Bu{defaultValue=null;_onChange=[];_pendingValue;_pendingChange=!1;constructor(n=null,e,i){super(CS(e),SS(i,e)),this._applyFormState(n),this._setUpdateStrategy(e),this._initObservables(),this.updateValueAndValidity({onlySelf:!0,emitEvent:!!this.asyncValidator}),$u(e)&&(e.nonNullable||e.initialValueIsDefault)&&(lS(n)?this.defaultValue=n.value:this.defaultValue=n)}setValue(n,e={}){tt(()=>{this.value=this._pendingValue=n,this._onChange.length&&e.emitModelToViewChange!==!1&&this._onChange.forEach(i=>i(this.value,e.emitViewToModelChange!==!1)),this.updateValueAndValidity(e)})}patchValue(n,e={}){this.setValue(n,e)}reset(n=this.defaultValue,e={}){this._applyFormState(n),this.markAsPristine(e),this.markAsUntouched(e),this.setValue(this.value,e),e.overwriteDefaultValue&&(this.defaultValue=this.value),this._pendingChange=!1,e?.emitEvent!==!1&&this._events.next(new Hr(this))}_updateValue(){}_anyControls(n){return!1}_allControlsDisabled(){return this.disabled}registerOnChange(n){this._onChange.push(n)}_unregisterOnChange(n){cS(this._onChange,n)}registerOnDisabledChange(n){this._onDisabledChange.push(n)}_unregisterOnDisabledChange(n){cS(this._onDisabledChange,n)}_forEachChild(n){}_syncPendingControls(){return this.updateOn===`submit`&&(this._pendingDirty&&this.markAsDirty(),this._pendingTouched&&this.markAsTouched(),this._pendingChange)?(this.setValue(this._pendingValue,{onlySelf:!0,emitModelToViewChange:!1}),!0):!1}_applyFormState(n){lS(n)?(this.value=this._pendingValue=n.value,n.disabled?this.disable({onlySelf:!0,emitEvent:!1}):this.enable({onlySelf:!0,emitEvent:!1})):this.value=this._pendingValue=n}};var sO=t=>t instanceof NS;var cO=(()=>{class t extends Oo{callSetDisabledState;get submitted(){return tt(this._submittedReactive)}set submitted(e){this._submittedReactive.set(e)}_submitted=ne(()=>this._submittedReactive());_submittedReactive=M(!1);_oldForm;_onCollectionChange=()=>this._updateDomValue();directives=[];constructor(e,i,a){super(),this.callSetDisabledState=a,this._setValidators(e),this._setAsyncValidators(i)}ngOnChanges(e){this.onChanges(e)}ngOnDestroy(){this.onDestroy()}onChanges(e){this._checkFormPresent(),Object.hasOwn(e,`form`)&&(this._updateValidators(),this._updateDomValue(),this._updateRegistrations(),this._oldForm=this.form)}onDestroy(){this.form&&(zu(this.form,this),this.form._onCollectionChange===this._onCollectionChange&&this.form._registerOnCollectionChange(()=>{}))}get formDirective(){return this}get path(){return[]}addControl(e){let i=this.form.get(e.path);return e._setupWithForm(i,this.callSetDisabledState),i.updateValueAndValidity({emitEvent:!1}),this.directives.push(e),i}getControl(e){return this.form.get(e.path)}removeControl(e){sS(e.control||null,e,!1),aO(this.directives,e)}addFormGroup(e){this._setUpFormContainer(e)}removeFormGroup(e){this._cleanUpFormContainer(e)}getFormGroup(e){return this.form.get(e.path)}getFormArray(e){return this.form.get(e.path)}addFormArray(e){this._setUpFormContainer(e)}removeFormArray(e){this._cleanUpFormContainer(e)}updateModel(e,i){this.form.get(e.path).setValue(i)}onReset(){this.resetForm()}resetForm(e=void 0,i={}){this.form.reset(e,i),this._submittedReactive.set(!1)}onSubmit(e){return this.submitted=!0,IS(this.form,this.directives),this.ngSubmit.emit(e),this.form._events.next(new Vu(this.control)),e?.target?.method===`dialog`}_updateDomValue(){this.directives.forEach(e=>{let i=e.control,a=this.form.get(e.path);i!==a&&(sS(i||null,e),sO(a)&&e._setupWithForm(a,this.callSetDisabledState))}),this.form._updateTreeValidity({emitEvent:!1})}_setUpFormContainer(e){let i=this.form.get(e.path);kS(i,e),i.updateValueAndValidity({emitEvent:!1})}_cleanUpFormContainer(e){let i=this.form?.get(e.path);i&&eO(i,e)&&i.updateValueAndValidity({emitEvent:!1})}_updateRegistrations(){this.form._registerOnCollectionChange(this._onCollectionChange),this._oldForm?._registerOnCollectionChange(()=>{})}_updateValidators(){Jg(this.form,this),this._oldForm&&zu(this._oldForm,this)}_checkFormPresent(){this.form}static ɵfac=function(i){return new(i||t)(je($r,10),je(Kg,10),je(Gu,8))};static ɵdir=q({type:t,features:[Fe,ft]})}return t})();var lO={provide:Oo,useExisting:zt(()=>Oc)};var Oc=(()=>{class t extends cO{form=null;ngSubmit=new J;get control(){return this.form}static ɵfac=(()=>{let e;return function(a){return(e||(e=It(t)))(a||t)}})();static ɵdir=q({type:t,selectors:[[``,`formGroup`,``]],hostBindings:function(i,a){i&1&&C(`submit`,function(r){return a.onSubmit(r)})(`reset`,function(){return a.onReset()})},inputs:{form:[0,`formGroup`,`form`]},outputs:{ngSubmit:`ngSubmit`},exportAs:[`ngForm`],standalone:!1,features:[$e([lO]),Fe]})}return t})();var dO={provide:Ii,useExisting:zt(()=>eb)};var dS=Promise.resolve();var eb=(()=>{class t extends Ii{_changeDetectorRef;callSetDisabledState;control=new NS;static ngAcceptInputType_isDisabled;_registered=!1;_ngModelInjector;viewModel;name=``;isDisabled;model;options;update=new J;constructor(e,i,a,o,r,s,c,m){super(c,m,o),this._changeDetectorRef=r,this.callSetDisabledState=s,this._parent=e,this._setValidators(i),this._setAsyncValidators(a)}ngOnChanges(e){if(this._registered,this._checkForErrors(),!this._registered||`name`in e){if(this._registered&&(this._checkName(),this.formDirective)){let i=e.name.previousValue;this.formDirective.removeControl({name:i,path:this._getPath(i)})}this._setUpControl()}`isDisabled`in e&&this._updateDisabled(e),tO(e,this.viewModel)&&(this._updateValue(this.model),this.viewModel=this.model)}ngOnDestroy(){this.formDirective?.removeControl(this)}ɵngControlCreate(e){super.ngControlCreate(e)}ɵngControlUpdate(e){super.ngControlUpdate(e,!1)}get shouldBindRequired(){return!1}get path(){return this._getPath(this.name)}get formDirective(){return this._parent?this._parent.formDirective:null}viewToModelUpdate(e){this.viewModel=e,this.update.emit(e)}_setUpControl(){this._setUpdateStrategy(),this._isStandalone()?this._setUpStandalone():this.formDirective.addControl(this),this._registered=!0}_setUpdateStrategy(){this.options&&this.options.updateOn!=null&&(this.control._updateOn=this.options.updateOn)}_isStandalone(){return!this._parent||!!(this.options&&this.options.standalone)}_setUpStandalone(){this.isCustomControlBased?this.setupCustomControl():(this.valueAccessor??=this.selectedValueAccessor,rS(this.control,this,this.callSetDisabledState)),this.control.updateValueAndValidity({emitEvent:!1})}_setupWithForm(e){this.isCustomControlBased?this.setupCustomControl():(this.valueAccessor??=this.selectedValueAccessor,rS(this.control,this,e))}_checkForErrors(){this._checkName()}_checkName(){this.options&&this.options.name&&(this.name=this.options.name),!this._isStandalone()&&this.name}_updateValue(e){dS.then(()=>{this.control.setValue(e,{emitViewToModelChange:!1}),this._changeDetectorRef?.markForCheck()})}_updateDisabled(e){let i=e.isDisabled.currentValue,a=i!==0&&V(i);dS.then(()=>{a&&!this.control.disabled?this.control.disable():!a&&this.control.disabled&&this.control.enable(),this._changeDetectorRef?.markForCheck()})}_getPath(e){return this._parent?KA(e,this._parent):[e]}static ɵfac=function(i){return new(i||t)(je(Oo,9),je($r,10),je(Kg,10),je(Ur,10),je(Re,8),je(Gu,8),je(re,8),je(Ae,8))};static ɵdir=q({type:t,selectors:[[``,`ngModel`,``,3,`formControlName`,``,3,`formControl`,``]],inputs:{name:`name`,isDisabled:[0,`disabled`,`isDisabled`],model:[0,`ngModel`,`model`],options:[0,`ngModelOptions`,`options`]},outputs:{update:`ngModelChange`},exportAs:[`ngModel`],standalone:!1,features:[$e([dO,oO]),Fe,ft,jf(null)]})}return t})();var uO=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({})}return t})();var TS=(()=>{class t{static withConfig(e){return{ngModule:t,providers:[{provide:Gu,useValue:e.callSetDisabledState??Zg}]}}static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({imports:[uO]})}return t})();var OS=Symbol(`FIELD_TREE`);function Rc(t){return typeof t==`function`&&t[OS]===!0}var Wu=new D(``);var Gr=(()=>{class t{isErrorState(e,i){return!!(e&&e.invalid&&(e.touched||i&&i.submitted))}isSignalErrorState(e){if(!e)return!1;let i=e().invalid(),a=e().touched();return i&&a}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var Wr=class{_defaultMatcher;_parentFormGroup;_parentForm;_stateChanges;errorState=!1;matcher;ngControl;formField;constructor(n,e,i,a,o){this._defaultMatcher=n,this._parentFormGroup=i,this._parentForm=a,this._stateChanges=o,e?on(e.field)&&!e.updateValueAndValidity?(this.formField=e,this.ngControl=null):(this.formField=null,this.ngControl=e):this.ngControl=this.formField=null}updateErrorState(){let n=this.errorState,e=this._getCurrentErrorState(this.matcher||this._defaultMatcher);e!==n&&(this.errorState=e,this._stateChanges.next())}_getCurrentErrorState(n){if(this.formField&&n?.isSignalErrorState)return n.isSignalErrorState(this.formField.field())??!1;let e=this._parentFormGroup||this._parentForm,i=this.ngControl?this.ngControl.control:null;return n?.isErrorState(i,e)??!1}};var tb=class{_box;_destroyed=new L;_resizeSubject=new L;_resizeObserver;_elementObservables=new Map;constructor(n){this._box=n,typeof ResizeObserver<`u`&&(this._resizeObserver=new ResizeObserver(e=>this._resizeSubject.next(e)))}observe(n){return this._elementObservables.has(n)||this._elementObservables.set(n,new xe(e=>{let i=this._resizeSubject.subscribe(e);return this._resizeObserver?.observe(n,{box:this._box}),()=>{this._resizeObserver?.unobserve(n),i.unsubscribe(),this._elementObservables.delete(n)}}).pipe(Ke(e=>e.some(i=>i.target===n)),Rl({bufferSize:1,refCount:!0}),Me(this._destroyed))),this._elementObservables.get(n)}destroy(){this._destroyed.next(),this._destroyed.complete(),this._resizeSubject.complete(),this._elementObservables.clear()}};var qu=(()=>{class t{_cleanupErrorListener;_observers=new Map;_ngZone=f(K);constructor(){}ngOnDestroy(){for(let[,e]of this._observers)e.destroy();this._observers.clear(),this._cleanupErrorListener?.()}observe(e,i){let a=i?.box||`content-box`;return this._observers.has(a)||this._observers.set(a,new tb(a)),this._observers.get(a).observe(e)}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var ta=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,selectors:[[`mat-label`]]})}return t})();var pO=new D(`MatError`);var nb=(()=>{class t{align=`start`;id=f(Le).getId(`mat-mdc-hint-`);static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,selectors:[[`mat-hint`]],hostAttrs:[1,`mat-mdc-form-field-hint`,`mat-mdc-form-field-bottom-align`],hostVars:4,hostBindings:function(i,a){i&2&&(Wt(`id`,a.id),ee(`align`,null),W(`mat-mdc-form-field-hint-end`,a.align===`end`))},inputs:{align:`align`,id:`id`}})}return t})();var hO=new D(`MatPrefix`);var fO=new D(`MatSuffix`);var zS=new D(`FloatingLabelParent`);var PS=(()=>{class t{_elementRef=f(G);get floating(){return this._floating}set floating(e){this._floating=e,this.monitorResize&&this._handleResize()}_floating=!1;get monitorResize(){return this._monitorResize}set monitorResize(e){this._monitorResize=e,this._monitorResize?this._subscribeToResize():this._resizeSubscription.unsubscribe()}_monitorResize=!1;_resizeObserver=f(qu);_ngZone=f(K);_parent=f(zS);_resizeSubscription=new De;ngOnDestroy(){this._resizeSubscription.unsubscribe()}getWidth(){return gO(this._elementRef.nativeElement)}get element(){return this._elementRef.nativeElement}_handleResize(){setTimeout(()=>this._parent._handleLabelResized())}_subscribeToResize(){this._resizeSubscription.unsubscribe(),this._ngZone.runOutsideAngular(()=>{this._resizeSubscription=this._resizeObserver.observe(this._elementRef.nativeElement,{box:`border-box`}).subscribe(()=>this._handleResize())})}static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,selectors:[[`label`,`matFormFieldFloatingLabel`,``]],hostAttrs:[1,`mdc-floating-label`,`mat-mdc-floating-label`],hostVars:2,hostBindings:function(i,a){i&2&&W(`mdc-floating-label--float-above`,a.floating)},inputs:{floating:`floating`,monitorResize:`monitorResize`}})}return t})();function gO(t){let n=t;if(n.offsetParent!==null)return n.scrollWidth;let e=n.cloneNode(!0);e.style.setProperty(`position`,`absolute`),e.style.setProperty(`transform`,`translate(-9999px, -9999px)`),document.documentElement.appendChild(e);let i=e.scrollWidth;return e.remove(),i}var FS=`mdc-line-ripple--active`;var Yu=`mdc-line-ripple--deactivating`;var LS=(()=>{class t{_elementRef=f(G);_cleanupTransitionEnd;constructor(){let e=f(K),i=f(Ae);e.runOutsideAngular(()=>{this._cleanupTransitionEnd=i.listen(this._elementRef.nativeElement,`transitionend`,this._handleTransitionEnd)})}activate(){let e=this._elementRef.nativeElement.classList;e.remove(Yu),e.add(FS)}deactivate(){this._elementRef.nativeElement.classList.add(Yu)}_handleTransitionEnd=e=>{let i=this._elementRef.nativeElement.classList,a=i.contains(Yu);e.propertyName===`opacity`&&a&&i.remove(FS,Yu)};ngOnDestroy(){this._cleanupTransitionEnd()}static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,selectors:[[`div`,`matFormFieldLineRipple`,``]],hostAttrs:[1,`mdc-line-ripple`]})}return t})();var VS=(()=>{class t{_elementRef=f(G);_ngZone=f(K);open=!1;_notch;ngAfterViewInit(){let e=this._elementRef.nativeElement,i=e.querySelector(`.mdc-floating-label`);i?(e.classList.add(`mdc-notched-outline--upgraded`),typeof requestAnimationFrame==`function`&&(i.style.transitionDuration=`0s`,this._ngZone.runOutsideAngular(()=>{requestAnimationFrame(()=>i.style.transitionDuration=``)}))):e.classList.add(`mdc-notched-outline--no-label`)}_setNotchWidth(e){let i=this._notch.nativeElement;!this.open||!e?i.style.width=``:i.style.width=`calc(${e}px * var(--mat-mdc-form-field-floating-label-scale, 0.75) + 9px)`}_setMaxWidth(e){this._notch.nativeElement.style.setProperty(`--mat-form-field-notch-max-width`,`calc(100% - ${e}px)`)}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`notch`];return B({type:t,selectors:[[`div`,`matFormFieldNotchedOutline`,``]],viewQuery:function(o,r){if(o&1&&Xe(e,5),o&2){let s;X(s=Z())&&(r._notch=s.first)}},hostAttrs:[1,`mdc-notched-outline`],hostVars:2,hostBindings:function(o,r){o&2&&W(`mdc-notched-outline--notched`,r.open)},inputs:{open:[0,`matFormFieldNotchedOutlineOpen`,`open`]},ngContentSelectors:[`*`],decls:5,vars:0,consts:[[`notch`,``],[1,`mat-mdc-notch-piece`,`mdc-notched-outline__leading`],[1,`mat-mdc-notch-piece`,`mdc-notched-outline__notch`],[1,`mat-mdc-notch-piece`,`mdc-notched-outline__trailing`]],template:function(o,r){o&1&&(Oe(),Gt(0,`div`,1),rt(1,`div`,2,0),ue(3),ut(),Gt(4,`div`,3))},encapsulation:2})})()}return t})();var Pc=(()=>{class t{id;ngField=null;ngControl=null;focused=!1;empty=!1;shouldLabelFloat=!1;required=!1;disabled=!1;errorState=!1;controlType;autofilled;userAriaDescribedBy;disableAutomaticLabeling;describedByIds;stateChanges=null;value;static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t})}return t})();var Fc=new D(`MatFormField`);var bO=new D(`MAT_FORM_FIELD_DEFAULT_OPTIONS`);var BS=`fill`;var vO=`auto`;var jS=`fixed`;var yO=`translateY(-50%)`;var Mi=(()=>{class t{_elementRef=f(G);_changeDetectorRef=f(Re);_platform=f(Te);_idGenerator=f(Le);_ngZone=f(K);_defaults=f(bO,{optional:!0});_currentDirection;_unwrapMaybeSignal(e){return on(e)?e():e}_textField;_iconPrefixContainer;_textPrefixContainer;_iconSuffixContainer;_textSuffixContainer;_floatingLabel;_notchedOutline;_lineRipple;_iconPrefixContainerSignal=sc(`iconPrefixContainer`);_textPrefixContainerSignal=sc(`textPrefixContainer`);_iconSuffixContainerSignal=sc(`iconSuffixContainer`);_textSuffixContainerSignal=sc(`textSuffixContainer`);_prefixSuffixContainers=ne(()=>[this._iconPrefixContainerSignal(),this._textPrefixContainerSignal(),this._iconSuffixContainerSignal(),this._textSuffixContainerSignal()].map(e=>e?.nativeElement).filter(e=>e!==void 0));_formFieldControl;_prefixChildren;_suffixChildren;_errorChildren;_hintChildren;_labelChild=fx(ta);get hideRequiredMarker(){return this._hideRequiredMarker}set hideRequiredMarker(e){this._hideRequiredMarker=ea(e)}_hideRequiredMarker=!1;color=`primary`;get floatLabel(){return this._floatLabel||this._defaults?.floatLabel||vO}set floatLabel(e){e!==this._floatLabel&&(this._floatLabel=e,this._changeDetectorRef.markForCheck())}_floatLabel;get appearance(){return this._appearanceSignal()}set appearance(e){let i=e||this._defaults?.appearance||BS;this._appearanceSignal.set(i)}_appearanceSignal=M(BS);get subscriptSizing(){return this._subscriptSizing||this._defaults?.subscriptSizing||jS}set subscriptSizing(e){this._subscriptSizing=e||this._defaults?.subscriptSizing||jS}_subscriptSizing=null;get hintLabel(){return this._hintLabel}set hintLabel(e){this._hintLabel=e,this._processHints()}_hintLabel=``;_hasIconPrefix=!1;_hasTextPrefix=!1;_hasIconSuffix=!1;_hasTextSuffix=!1;_labelId=this._idGenerator.getId(`mat-mdc-form-field-label-`);_hintLabelId=this._idGenerator.getId(`mat-mdc-hint-`);_describedByIds;get _control(){return this._explicitFormFieldControl||this._formFieldControl}set _control(e){this._explicitFormFieldControl=e}_destroyed=new L;_isFocused=null;_explicitFormFieldControl;_previousControl=null;_previousControlValidatorFn=null;_stateChanges;_valueChanges;_describedByChanges;_outlineLabelOffsetResizeObserver=null;_animationsDisabled=ze();constructor(){let e=this._defaults,i=f(pt);e&&(e.appearance&&(this.appearance=e.appearance),this._hideRequiredMarker=!!e?.hideRequiredMarker,e.color&&(this.color=e.color)),Pt(()=>this._currentDirection=i.valueSignal()),this._syncOutlineLabelOffset()}ngAfterViewInit(){this._updateFocusState(),this._animationsDisabled||this._ngZone.runOutsideAngular(()=>{setTimeout(()=>{this._elementRef.nativeElement.classList.add(`mat-form-field-animations-enabled`)},300)}),this._changeDetectorRef.detectChanges()}ngAfterContentInit(){this._assertFormFieldControl(),this._initializeSubscript(),this._initializePrefixAndSuffix()}ngAfterContentChecked(){this._assertFormFieldControl(),this._control!==this._previousControl&&(this._initializeControl(this._previousControl),this._control.ngControl&&this._control.ngControl.control&&(this._previousControlValidatorFn=Rc(this._control.ngField)?null:this._control.ngControl.control.validator),this._previousControl=this._control,this._changeDetectorRef.markForCheck()),this._control.ngControl&&this._control.ngControl.control&&!Rc(this._control.ngField)&&this._control.ngControl.control.validator!==this._previousControlValidatorFn&&this._changeDetectorRef.markForCheck()}ngOnDestroy(){this._outlineLabelOffsetResizeObserver?.disconnect(),this._stateChanges?.unsubscribe(),this._valueChanges?.unsubscribe(),this._describedByChanges?.unsubscribe(),this._destroyed.next(),this._destroyed.complete()}getLabelId=ne(()=>this._hasFloatingLabel()?this._labelId:null);getConnectedOverlayOrigin(){return this._textField||this._elementRef}_animateAndLockLabel(){this._hasFloatingLabel()&&(this.floatLabel=`always`)}_initializeControl(e){let i=this._control,a=`mat-mdc-form-field-type-`;e&&this._elementRef.nativeElement.classList.remove(a+e.controlType),i.controlType&&this._elementRef.nativeElement.classList.add(a+i.controlType),this._stateChanges?.unsubscribe(),this._stateChanges=i.stateChanges?.subscribe(()=>{this._updateFocusState(),this._changeDetectorRef.markForCheck()}),this._describedByChanges?.unsubscribe(),this._describedByChanges=i.stateChanges?.pipe(dt([void 0,void 0]),ot(()=>[this._unwrapMaybeSignal(i.errorState),i.userAriaDescribedBy]),Ol(),Ke(([[o,r],[s,c]])=>o!==s||r!==c)).subscribe(()=>this._syncDescribedByIds()),this._valueChanges?.unsubscribe(),i.ngControl&&i.ngControl.valueChanges&&!Rc(i.ngField)&&(this._valueChanges=i.ngControl.valueChanges.pipe(Me(this._destroyed)).subscribe(()=>this._changeDetectorRef.markForCheck()))}_checkPrefixAndSuffixTypes(){this._hasIconPrefix=!!this._prefixChildren.find(e=>!e._isText),this._hasTextPrefix=!!this._prefixChildren.find(e=>e._isText),this._hasIconSuffix=!!this._suffixChildren.find(e=>!e._isText),this._hasTextSuffix=!!this._suffixChildren.find(e=>e._isText)}_initializePrefixAndSuffix(){this._checkPrefixAndSuffixTypes(),wt(this._prefixChildren.changes,this._suffixChildren.changes).subscribe(()=>{this._checkPrefixAndSuffixTypes(),this._changeDetectorRef.markForCheck()})}_initializeSubscript(){this._hintChildren.changes.subscribe(()=>{this._processHints(),this._changeDetectorRef.markForCheck()}),this._errorChildren.changes.subscribe(()=>{this._syncDescribedByIds(),this._changeDetectorRef.markForCheck()}),this._validateHints(),this._syncDescribedByIds()}_assertFormFieldControl(){this._control}_updateFocusState(){let e=this._unwrapMaybeSignal(this._control.focused);e&&!this._isFocused?(this._isFocused=!0,this._lineRipple?.activate()):!e&&(this._isFocused||this._isFocused===null)&&(this._isFocused=!1,this._lineRipple?.deactivate()),this._elementRef.nativeElement.classList.toggle(`mat-focused`,e),this._textField?.nativeElement.classList.toggle(`mdc-text-field--focused`,e)}_syncOutlineLabelOffset(){eg({earlyRead:()=>{if(this._appearanceSignal()!==`outline`)return this._outlineLabelOffsetResizeObserver?.disconnect(),null;if(globalThis.ResizeObserver){this._outlineLabelOffsetResizeObserver||=new globalThis.ResizeObserver(()=>{this._writeOutlinedLabelStyles(this._getOutlinedLabelOffset())});for(let e of this._prefixSuffixContainers())this._outlineLabelOffsetResizeObserver.observe(e,{box:`border-box`})}return this._getOutlinedLabelOffset()},write:e=>this._writeOutlinedLabelStyles(e())})}_shouldAlwaysFloat(){return this.floatLabel===`always`}_hasOutline(){return this.appearance===`outline`}_forceDisplayInfixLabel(){return!this._platform.isBrowser&&this._prefixChildren.length&&!this._shouldLabelFloat()}_hasFloatingLabel=ne(()=>!!this._labelChild());_shouldLabelFloat(){return this._hasFloatingLabel()?this._shouldAlwaysFloat()||this._unwrapMaybeSignal(this._control.shouldLabelFloat):!1}_shouldForward(e){let i=this._control?.ngField||this._control?.ngControl;if(!i)return!1;if(Rc(i)){let a=i();return e===`valid`?a.valid():e===`dirty`?a.dirty():e===`touched`?a.touched():e===`pending`?a.pending():e===`untouched`?!a.touched():e===`pristine`?!a.dirty():e===`invalid`?!a.valid():!1}else{let a=i;return e===`valid`?a.valid:e===`dirty`?a.dirty:e===`touched`?a.touched:e===`pending`?a.pending:e===`untouched`?a.untouched:e===`pristine`?a.pristine:e===`invalid`?a.invalid:!1}}_getSubscriptMessageType(){return this._errorChildren&&this._errorChildren.length>0&&this._unwrapMaybeSignal(this._control.errorState)?`error`:`hint`}_handleLabelResized(){this._refreshOutlineNotchWidth()}_refreshOutlineNotchWidth(){!this._hasOutline()||!this._floatingLabel||!this._shouldLabelFloat()?this._notchedOutline?._setNotchWidth(0):this._notchedOutline?._setNotchWidth(this._floatingLabel.getWidth())}_processHints(){this._validateHints(),this._syncDescribedByIds()}_validateHints(){this._hintChildren}_syncDescribedByIds(){if(this._control){let e=[];if(this._control.userAriaDescribedBy&&typeof this._control.userAriaDescribedBy==`string`&&e.push(...this._control.userAriaDescribedBy.split(` `)),this._getSubscriptMessageType()===`hint`){let o=this._hintChildren?this._hintChildren.find(s=>s.align===`start`):null,r=this._hintChildren?this._hintChildren.find(s=>s.align===`end`):null;o?e.push(o.id):this._hintLabel&&e.push(this._hintLabelId),r&&e.push(r.id)}else this._errorChildren&&e.push(...this._errorChildren.map(o=>o.id));let i=this._control.describedByIds,a;if(i){let o=this._describedByIds||e;a=e.concat(i.filter(r=>r&&!o.includes(r)))}else a=e;this._control.setDescribedByIds(a),this._describedByIds=e}}_getOutlinedLabelOffset(){if(!this._hasOutline()||!this._floatingLabel)return null;if(!this._iconPrefixContainer&&!this._textPrefixContainer)return[``,null];if(!this._isAttachedToDom())return null;let e=this._iconPrefixContainer?.nativeElement,i=this._textPrefixContainer?.nativeElement,a=this._iconSuffixContainer?.nativeElement,o=this._textSuffixContainer?.nativeElement,r=e?.getBoundingClientRect().width??0,s=i?.getBoundingClientRect().width??0,c=a?.getBoundingClientRect().width??0,m=o?.getBoundingClientRect().width??0;return[`var(--mat-mdc-form-field-label-transform, ${yO} translateX(${`calc(${this._currentDirection===`rtl`?`-1`:`1`} * (${`${r+s}px`} + var(--mat-mdc-form-field-label-offset-x, 0px)))`}))`,r+s+c+m]}_writeOutlinedLabelStyles(e){if(e!==null){let[i,a]=e;this._floatingLabel&&(this._floatingLabel.element.style.transform=i),a!==null&&this._notchedOutline?._setMaxWidth(a)}}_isAttachedToDom(){let e=this._elementRef.nativeElement;if(e.getRootNode){let i=e.getRootNode();return i&&i!==e}return document.documentElement.contains(e)}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`iconPrefixContainer`],i=[`textPrefixContainer`],a=[`iconSuffixContainer`],o=[`textSuffixContainer`],r=[`textField`],s=[`*`,[[`mat-label`]],[[``,`matPrefix`,``],[``,`matIconPrefix`,``]],[[``,`matTextPrefix`,``]],[[``,`matTextSuffix`,``]],[[``,`matSuffix`,``],[``,`matIconSuffix`,``]],[[`mat-error`],[``,`matError`,``]],[[`mat-hint`,3,`align`,`end`]],[[`mat-hint`,`align`,`end`]]],c=[`*`,`mat-label`,`[matPrefix], [matIconPrefix]`,`[matTextPrefix]`,`[matTextSuffix]`,`[matSuffix], [matIconSuffix]`,`mat-error, [matError]`,`mat-hint:not([align='end'])`,`mat-hint[align='end']`];function m(le,ke){le&1&&z(0,`span`,21)}function u(le,ke){if(le&1&&(l(0,`label`,20),ue(1,1),N(2,m,1,0,`span`,21),d()),le&2){let F=_(2);S(`floating`,F._shouldLabelFloat())(`monitorResize`,F._hasOutline())(`id`,F._labelId),ee(`for`,F._control.disableAutomaticLabeling?null:F._control.id),h(2),T(!F.hideRequiredMarker&&F._unwrapMaybeSignal(F._control.required)?2:-1)}}function g(le,ke){if(le&1&&N(0,u,3,5,`label`,20),le&2)T(_()._hasFloatingLabel()?0:-1)}function v(le,ke){le&1&&z(0,`div`,7)}function b(le,ke){}function y(le,ke){if(le&1&&at(0,b,0,0,`ng-template`,13),le&2){_(2);S(`ngTemplateOutlet`,mt(1))}}function E(le,ke){if(le&1&&(l(0,`div`,9),N(1,y,1,1,null,13),d()),le&2){let F=_();S(`matFormFieldNotchedOutlineOpen`,F._shouldLabelFloat()),h(),T(F._forceDisplayInfixLabel()?-1:1)}}function R(le,ke){le&1&&(l(0,`div`,10,2),ue(2,2),d())}function oe(le,ke){le&1&&(l(0,`div`,11,3),ue(2,3),d())}function j(le,ke){}function x(le,ke){if(le&1&&at(0,j,0,0,`ng-template`,13),le&2){_();S(`ngTemplateOutlet`,mt(1))}}function k(le,ke){le&1&&(l(0,`div`,14,4),ue(2,4),d())}function I(le,ke){le&1&&(l(0,`div`,15,5),ue(2,5),d())}function U(le,ke){le&1&&z(0,`div`,16)}function ge(le,ke){le&1&&(l(0,`div`,18),ue(1,6),d())}function ht(le,ke){if(le&1&&(l(0,`mat-hint`,22),p(1),d()),le&2){let F=_(2);S(`id`,F._hintLabelId),h(),w(F.hintLabel)}}function Ge(le,ke){if(le&1&&(l(0,`div`,19),N(1,ht,2,2,`mat-hint`,22),ue(2,7),z(3,`div`,23),ue(4,8),d()),le&2){let F=_();h(),T(F.hintLabel?1:-1)}}return B({type:t,selectors:[[`mat-form-field`]],contentQueries:function(ke,F,We){if(ke&1&&($d(We,F._labelChild,ta,5),Vt(We,Pc,5)(We,hO,5)(We,fO,5)(We,pO,5)(We,nb,5)),ke&2){Wd();let en;X(en=Z())&&(F._formFieldControl=en.first),X(en=Z())&&(F._prefixChildren=en),X(en=Z())&&(F._suffixChildren=en),X(en=Z())&&(F._errorChildren=en),X(en=Z())&&(F._hintChildren=en)}},viewQuery:function(ke,F){if(ke&1&&(Gd(F._iconPrefixContainerSignal,e,5)(F._textPrefixContainerSignal,i,5)(F._iconSuffixContainerSignal,a,5)(F._textSuffixContainerSignal,o,5),Xe(r,5)(e,5)(i,5)(a,5)(o,5)(PS,5)(VS,5)(LS,5)),ke&2){Wd(4);let We;X(We=Z())&&(F._textField=We.first),X(We=Z())&&(F._iconPrefixContainer=We.first),X(We=Z())&&(F._textPrefixContainer=We.first),X(We=Z())&&(F._iconSuffixContainer=We.first),X(We=Z())&&(F._textSuffixContainer=We.first),X(We=Z())&&(F._floatingLabel=We.first),X(We=Z())&&(F._notchedOutline=We.first),X(We=Z())&&(F._lineRipple=We.first)}},hostAttrs:[1,`mat-mdc-form-field`],hostVars:38,hostBindings:function(ke,F){ke&2&&W(`mat-mdc-form-field-label-always-float`,F._shouldAlwaysFloat())(`mat-mdc-form-field-has-icon-prefix`,F._hasIconPrefix)(`mat-mdc-form-field-has-icon-suffix`,F._hasIconSuffix)(`mat-form-field-invalid`,F._unwrapMaybeSignal(F._control.errorState))(`mat-form-field-disabled`,F._unwrapMaybeSignal(F._control.disabled))(`mat-form-field-autofilled`,F._unwrapMaybeSignal(F._control.autofilled))(`mat-form-field-appearance-fill`,F.appearance==`fill`)(`mat-form-field-appearance-outline`,F.appearance==`outline`)(`mat-form-field-hide-placeholder`,F._hasFloatingLabel()&&!F._shouldLabelFloat())(`mat-primary`,F.color!==`accent`&&F.color!==`warn`)(`mat-accent`,F.color===`accent`)(`mat-warn`,F.color===`warn`)(`ng-untouched`,F._shouldForward(`untouched`))(`ng-touched`,F._shouldForward(`touched`))(`ng-pristine`,F._shouldForward(`pristine`))(`ng-dirty`,F._shouldForward(`dirty`))(`ng-valid`,F._shouldForward(`valid`))(`ng-invalid`,F._shouldForward(`invalid`))(`ng-pending`,F._shouldForward(`pending`))},inputs:{hideRequiredMarker:`hideRequiredMarker`,color:`color`,floatLabel:`floatLabel`,appearance:`appearance`,subscriptSizing:`subscriptSizing`,hintLabel:`hintLabel`},exportAs:[`matFormField`],features:[$e([{provide:Fc,useExisting:t},{provide:zS,useExisting:t}])],ngContentSelectors:c,decls:18,vars:21,consts:[[`labelTemplate`,``],[`textField`,``],[`iconPrefixContainer`,``],[`textPrefixContainer`,``],[`textSuffixContainer`,``],[`iconSuffixContainer`,``],[1,`mat-mdc-text-field-wrapper`,`mdc-text-field`,3,`click`],[1,`mat-mdc-form-field-focus-overlay`],[1,`mat-mdc-form-field-flex`],[`matFormFieldNotchedOutline`,``,3,`matFormFieldNotchedOutlineOpen`],[1,`mat-mdc-form-field-icon-prefix`],[1,`mat-mdc-form-field-text-prefix`],[1,`mat-mdc-form-field-infix`],[3,`ngTemplateOutlet`],[1,`mat-mdc-form-field-text-suffix`],[1,`mat-mdc-form-field-icon-suffix`],[`matFormFieldLineRipple`,``],[`aria-atomic`,`true`,`aria-live`,`polite`,1,`mat-mdc-form-field-subscript-wrapper`,`mat-mdc-form-field-bottom-align`],[1,`mat-mdc-form-field-error-wrapper`],[1,`mat-mdc-form-field-hint-wrapper`],[`matFormFieldFloatingLabel`,``,3,`floating`,`monitorResize`,`id`],[`aria-hidden`,`true`,1,`mat-mdc-form-field-required-marker`,`mdc-floating-label--required`],[3,`id`],[1,`mat-mdc-form-field-hint-spacer`]],template:function(ke,F){if(ke&1&&(Oe(s),at(0,g,1,1,`ng-template`,null,0,ac),l(2,`div`,6,1),C(`click`,function(en){return F._control.onContainerClick(en)}),N(4,v,1,0,`div`,7),l(5,`div`,8),N(6,E,2,2,`div`,9),N(7,R,3,0,`div`,10),N(8,oe,3,0,`div`,11),l(9,`div`,12),N(10,x,1,1,null,13),ue(11),d(),N(12,k,3,0,`div`,14),N(13,I,3,0,`div`,15),d(),N(14,U,1,0,`div`,16),d(),l(15,`div`,17),N(16,ge,2,0,`div`,18)(17,Ge,5,1,`div`,19),d()),ke&2){let We,en=F._unwrapMaybeSignal(F._control.disabled);h(2),W(`mdc-text-field--filled`,!F._hasOutline())(`mdc-text-field--outlined`,F._hasOutline())(`mdc-text-field--no-label`,!F._hasFloatingLabel())(`mdc-text-field--disabled`,en)(`mdc-text-field--invalid`,F._unwrapMaybeSignal(F._control.errorState)),h(2),T(!F._hasOutline()&&!en?4:-1),h(2),T(F._hasOutline()?6:-1),h(),T(F._hasIconPrefix?7:-1),h(),T(F._hasTextPrefix?8:-1),h(2),T(!F._hasOutline()||F._forceDisplayInfixLabel()?10:-1),h(2),T(F._hasTextSuffix?12:-1),h(),T(F._hasIconSuffix?13:-1),h(),T(F._hasOutline()?-1:14),h(),W(`mat-mdc-form-field-subscript-dynamic-size`,F.subscriptSizing===`dynamic`);let iE=F._getSubscriptMessageType();h(),T((We=iE)===`error`?16:We===`hint`?17:-1)}},dependencies:[PS,VS,pc,LS,nb],styles:[`.mdc-text-field {
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
`],encapsulation:2})})()}return t})();var _O=new D(`mat-chips-default-options`,{providedIn:`root`,factory:()=>({separatorKeyCodes:[13]})});var HS=new D(`MatChipAvatar`);var US=new D(`MatChipTrailingIcon`);var $S=new D(`MatChipEdit`);var GS=new D(`MatChipRemove`);var WS=new D(`MatChip`);var qS=(()=>{class t{_elementRef=f(G);_parentChip=f(WS);_isPrimary=!0;_isLeading=!1;get disabled(){return this._disabled||this._parentChip?.disabled||!1}set disabled(e){this._disabled=e}_disabled=!1;tabIndex=-1;_allowFocusWhenDisabled=!1;_getDisabledAttribute(){return this.disabled&&!this._allowFocusWhenDisabled?``:null}constructor(){f(Ze).load(cn),this._elementRef.nativeElement.nodeName===`BUTTON`&&this._elementRef.nativeElement.setAttribute(`type`,`button`)}focus(){this._elementRef.nativeElement.focus()}static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,selectors:[[``,`matChipContent`,``]],hostAttrs:[1,`mat-mdc-chip-action`,`mdc-evolution-chip__action`,`mdc-evolution-chip__action--presentational`],hostVars:8,hostBindings:function(i,a){i&2&&(ee(`disabled`,a._getDisabledAttribute())(`aria-disabled`,a.disabled),W(`mdc-evolution-chip__action--primary`,a._isPrimary)(`mdc-evolution-chip__action--secondary`,!a._isPrimary)(`mdc-evolution-chip__action--trailing`,!a._isPrimary&&!a._isLeading))},inputs:{disabled:[2,`disabled`,`disabled`,V],tabIndex:[2,`tabIndex`,`tabIndex`,e=>e==null?-1:st(e)],_allowFocusWhenDisabled:`_allowFocusWhenDisabled`}})}return t})();var xO=(()=>{class t extends qS{_getTabindex(){return this.disabled&&!this._allowFocusWhenDisabled?null:this.tabIndex.toString()}_handleClick(e){!this.disabled&&this._isPrimary&&(e.preventDefault(),this._parentChip._handlePrimaryActionInteraction())}_handleKeydown(e){(e.keyCode===13||e.keyCode===32)&&!this.disabled&&this._isPrimary&&!this._parentChip._isEditing&&(e.preventDefault(),this._parentChip._handlePrimaryActionInteraction())}static ɵfac=(()=>{let e;return function(a){return(e||(e=It(t)))(a||t)}})();static ɵdir=q({type:t,selectors:[[``,`matChipAction`,``]],hostVars:3,hostBindings:function(i,a){i&1&&C(`click`,function(r){return a._handleClick(r)})(`keydown`,function(r){return a._handleKeydown(r)}),i&2&&(ee(`tabindex`,a._getTabindex()),W(`mdc-evolution-chip__action--presentational`,!1))},features:[Fe]})}return t})();var Lc=(()=>{class t{_changeDetectorRef=f(Re);_elementRef=f(G);_tagName=f(mx);_ngZone=f(K);_focusMonitor=f(Xt);_globalRippleOptions=f(Vr,{optional:!0});_document=f(ce);_onFocus=new L;_onBlur=new L;_isBasicChip=!1;role=null;_hasFocusInternal=!1;_pendingFocus=!1;_actionChanges;_animationsDisabled=ze();_allLeadingIcons;_allTrailingIcons;_allEditIcons;_allRemoveIcons;_hasFocus(){return this._hasFocusInternal}id=f(Le).getId(`mat-mdc-chip-`);ariaLabel=null;ariaDescription=null;_chipListDisabled=!1;_hadFocusOnRemove=!1;_textElement;get value(){return this._value!==void 0?this._value:this._textElement.textContent.trim()}set value(e){this._value=e}_value;color;removable=!0;highlighted=!1;disableRipple=!1;get disabled(){return this._disabled||this._chipListDisabled}set disabled(e){this._disabled=e}_disabled=!1;removed=new J;destroyed=new J;basicChipAttrName=`mat-basic-chip`;leadingIcon;editIcon;trailingIcon;removeIcon;primaryAction;_rippleLoader=f(Tu);_injector=f(re);constructor(){let e=f(Ze);e.load(cn),e.load(Di),this._monitorFocus(),this._rippleLoader?.configureRipple(this._elementRef.nativeElement,{className:`mat-mdc-chip-ripple`,disabled:this._isRippleDisabled()})}ngOnInit(){this._isBasicChip=this._elementRef.nativeElement.hasAttribute(this.basicChipAttrName)||this._tagName.toLowerCase()===this.basicChipAttrName}ngAfterViewInit(){this._textElement=this._elementRef.nativeElement.querySelector(`.mat-mdc-chip-action-label`),this._pendingFocus&&(this._pendingFocus=!1,this.focus())}ngAfterContentInit(){this._actionChanges=wt(this._allLeadingIcons.changes,this._allTrailingIcons.changes,this._allEditIcons.changes,this._allRemoveIcons.changes).subscribe(()=>this._changeDetectorRef.markForCheck())}ngDoCheck(){this._rippleLoader.setDisabled(this._elementRef.nativeElement,this._isRippleDisabled())}ngOnDestroy(){this.destroyed.emit({chip:this}),this.destroyed.complete(),this._focusMonitor.stopMonitoring(this._elementRef),this._rippleLoader?.destroyRipple(this._elementRef.nativeElement),this._actionChanges?.unsubscribe()}remove(){this.removable&&(this._hadFocusOnRemove=this._hasFocus(),this.removed.emit({chip:this}))}_isRippleDisabled(){return this.disabled||this.disableRipple||this._animationsDisabled||this._isBasicChip||!this._hasInteractiveActions()||!!this._globalRippleOptions?.disabled}_hasTrailingIcon(){return!!(this.trailingIcon||this.removeIcon)}_handleKeydown(e){(e.keyCode===8&&!e.repeat||e.keyCode===46)&&(e.preventDefault(),this.remove())}focus(){this.disabled||(this.primaryAction?this.primaryAction.focus():this._pendingFocus=!0)}_getSourceAction(e){return this._getActions().find(i=>{let a=i._elementRef.nativeElement;return a===e||a.contains(e)})}_getActions(){let e=[];return this.editIcon&&e.push(this.editIcon),this.primaryAction&&e.push(this.primaryAction),this.removeIcon&&e.push(this.removeIcon),e}_handlePrimaryActionInteraction(){}_hasInteractiveActions(){return this._getActions().length>0}_edit(e){}_monitorFocus(){this._focusMonitor.monitor(this._elementRef,!0).subscribe(e=>{let i=e!==null;i!==this._hasFocusInternal&&(this._hasFocusInternal=i,i?this._onFocus.next({chip:this}):(this._changeDetectorRef.markForCheck(),setTimeout(()=>this._ngZone.run(()=>this._onBlur.next({chip:this})))))})}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`,[[`mat-chip-avatar`],[``,`matChipAvatar`,``]],[[`mat-chip-trailing-icon`],[``,`matChipRemove`,``],[``,`matChipTrailingIcon`,``]]],i=[`*`,`mat-chip-avatar, [matChipAvatar]`,`mat-chip-trailing-icon,[matChipRemove],[matChipTrailingIcon]`];function a(r,s){r&1&&(l(0,`span`,3),ue(1,1),d())}function o(r,s){r&1&&(l(0,`span`,6),ue(1,2),d())}return B({type:t,selectors:[[`mat-basic-chip`],[``,`mat-basic-chip`,``],[`mat-chip`],[``,`mat-chip`,``]],contentQueries:function(s,c,m){if(s&1&&Vt(m,HS,5)(m,$S,5)(m,US,5)(m,GS,5)(m,HS,5)(m,US,5)(m,$S,5)(m,GS,5),s&2){let u;X(u=Z())&&(c.leadingIcon=u.first),X(u=Z())&&(c.editIcon=u.first),X(u=Z())&&(c.trailingIcon=u.first),X(u=Z())&&(c.removeIcon=u.first),X(u=Z())&&(c._allLeadingIcons=u),X(u=Z())&&(c._allTrailingIcons=u),X(u=Z())&&(c._allEditIcons=u),X(u=Z())&&(c._allRemoveIcons=u)}},viewQuery:function(s,c){if(s&1&&Xe(xO,5),s&2){let m;X(m=Z())&&(c.primaryAction=m.first)}},hostAttrs:[1,`mat-mdc-chip`],hostVars:31,hostBindings:function(s,c){s&1&&C(`keydown`,function(u){return c._handleKeydown(u)}),s&2&&(Wt(`id`,c.id),ee(`role`,c.role)(`aria-label`,c.ariaLabel),et(`mat-`+(c.color||`primary`)),W(`mdc-evolution-chip`,!c._isBasicChip)(`mdc-evolution-chip--disabled`,c.disabled)(`mdc-evolution-chip--with-trailing-action`,c._hasTrailingIcon())(`mdc-evolution-chip--with-primary-graphic`,c.leadingIcon)(`mdc-evolution-chip--with-primary-icon`,c.leadingIcon)(`mdc-evolution-chip--with-avatar`,c.leadingIcon)(`mat-mdc-chip-with-avatar`,c.leadingIcon)(`mat-mdc-chip-highlighted`,c.highlighted)(`mat-mdc-chip-disabled`,c.disabled)(`mat-mdc-basic-chip`,c._isBasicChip)(`mat-mdc-standard-chip`,!c._isBasicChip)(`mat-mdc-chip-with-trailing-icon`,c._hasTrailingIcon())(`_mat-animation-noopable`,c._animationsDisabled))},inputs:{role:`role`,id:`id`,ariaLabel:[0,`aria-label`,`ariaLabel`],ariaDescription:[0,`aria-description`,`ariaDescription`],value:`value`,color:`color`,removable:[2,`removable`,`removable`,V],highlighted:[2,`highlighted`,`highlighted`,V],disableRipple:[2,`disableRipple`,`disableRipple`,V],disabled:[2,`disabled`,`disabled`,V]},outputs:{removed:`removed`,destroyed:`destroyed`},exportAs:[`matChip`],features:[$e([{provide:WS,useExisting:t}])],ngContentSelectors:i,decls:8,vars:2,consts:[[1,`mat-mdc-chip-focus-overlay`],[1,`mdc-evolution-chip__cell`,`mdc-evolution-chip__cell--primary`],[`matChipContent`,``],[1,`mdc-evolution-chip__graphic`,`mat-mdc-chip-graphic`],[1,`mdc-evolution-chip__text-label`,`mat-mdc-chip-action-label`],[1,`mat-mdc-chip-primary-focus-indicator`,`mat-focus-indicator`],[1,`mdc-evolution-chip__cell`,`mdc-evolution-chip__cell--trailing`]],template:function(s,c){s&1&&(Oe(e),z(0,`span`,0),l(1,`span`,1)(2,`span`,2),N(3,a,2,0,`span`,3),l(4,`span`,4),ue(5),z(6,`span`,5),d()()(),N(7,o,2,0,`span`,6)),s&2&&(h(3),T(c.leadingIcon?3:-1),h(4),T(c._hasTrailingIcon()?7:-1))},dependencies:[qS],styles:[`.mdc-evolution-chip,
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
`],encapsulation:2})})()}return t})();var Ku=(()=>{class t{_elementRef=f(G);_changeDetectorRef=f(Re);_dir=f(pt,{optional:!0});_lastDestroyedFocusedChipIndex=null;_keyManager;_destroyed=new L;_defaultRole=`presentation`;get chipFocusChanges(){return this._getChipStream(e=>e._onFocus)}get chipDestroyedChanges(){return this._getChipStream(e=>e.destroyed)}get chipRemovedChanges(){return this._getChipStream(e=>e.removed)}get disabled(){return this._disabled}set disabled(e){this._disabled=e,this._syncChipsState()}_disabled=!1;get empty(){return!this._chips||this._chips.length===0}get role(){return this._explicitRole?this._explicitRole:this.empty?null:this._defaultRole}tabIndex=0;set role(e){this._explicitRole=e}_explicitRole=null;get focused(){return this._hasFocusedChip()}_chips;_chipActions=new _n;ngAfterViewInit(){this._setUpFocusManagement(),this._trackChipSetChanges(),this._trackDestroyedFocusedChip()}ngOnDestroy(){this._keyManager?.destroy(),this._chipActions.destroy(),this._destroyed.next(),this._destroyed.complete()}_hasFocusedChip(){return this._chips&&this._chips.some(e=>e._hasFocus())}_syncChipsState(){this._chips?.forEach(e=>{e._chipListDisabled=this._disabled,e._changeDetectorRef.markForCheck()})}focus(){}_handleKeydown(e){this._originatesFromChip(e)&&this._keyManager.onKeydown(e)}_isValidIndex(e){return e>=0&&e<this._chips.length}_allowFocusEscape(){let e=this._elementRef.nativeElement.tabIndex;e!==-1&&(this._elementRef.nativeElement.tabIndex=-1,setTimeout(()=>this._elementRef.nativeElement.tabIndex=e))}_getChipStream(e){return this._chips.changes.pipe(dt(null),fn(()=>wt(...this._chips.map(e))))}_originatesFromChip(e){let i=e.target;for(;i&&i!==this._elementRef.nativeElement;){if(i.classList.contains(`mat-mdc-chip`))return!0;i=i.parentElement}return!1}_setUpFocusManagement(){this._chips.changes.pipe(dt(this._chips)).subscribe(e=>{let i=[];e.forEach(a=>a._getActions().forEach(o=>i.push(o))),this._chipActions.reset(i),this._chipActions.notifyOnChanges()}),this._keyManager=new Ji(this._chipActions).withVerticalOrientation().withHorizontalOrientation(this._dir?this._dir.value:`ltr`).withHomeAndEnd().skipPredicate(e=>this._skipPredicate(e)),this.chipFocusChanges.pipe(Me(this._destroyed)).subscribe(({chip:e})=>{let i=e._getSourceAction(document.activeElement);i&&this._keyManager.updateActiveItem(i)}),this._dir?.change.pipe(Me(this._destroyed)).subscribe(e=>this._keyManager.withHorizontalOrientation(e))}_skipPredicate(e){return e.disabled}_trackChipSetChanges(){this._chips.changes.pipe(dt(null),Me(this._destroyed)).subscribe(()=>{this.disabled&&Promise.resolve().then(()=>this._syncChipsState()),this._redirectDestroyedChipFocus()})}_trackDestroyedFocusedChip(){this.chipDestroyedChanges.pipe(Me(this._destroyed)).subscribe(e=>{let a=this._chips.toArray().indexOf(e.chip),o=e.chip._hasFocus(),r=e.chip._hadFocusOnRemove&&this._keyManager.activeItem&&e.chip._getActions().includes(this._keyManager.activeItem),s=o||r;this._isValidIndex(a)&&s&&(this._lastDestroyedFocusedChipIndex=a)})}_redirectDestroyedChipFocus(){if(this._lastDestroyedFocusedChipIndex!=null){if(this._chips.length){let e=Math.min(this._lastDestroyedFocusedChipIndex,this._chips.length-1),i=this._chips.toArray()[e];i.disabled?this._chips.length===1?this.focus():this._keyManager.setPreviousItemActive():i.focus()}else this.focus();this._lastDestroyedFocusedChipIndex=null}}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){return B({type:t,selectors:[[`mat-chip-set`]],contentQueries:function(a,o,r){if(a&1&&Vt(r,Lc,5),a&2){let s;X(s=Z())&&(o._chips=s)}},hostAttrs:[1,`mat-mdc-chip-set`,`mdc-evolution-chip-set`],hostVars:1,hostBindings:function(a,o){a&1&&C(`keydown`,function(s){return o._handleKeydown(s)}),a&2&&ee(`role`,o.role)},inputs:{disabled:[2,`disabled`,`disabled`,V],role:`role`,tabIndex:[2,`tabIndex`,`tabIndex`,i=>i==null?0:st(i)]},ngContentSelectors:[`*`],decls:2,vars:0,consts:[[`role`,`presentation`,1,`mdc-evolution-chip-set__chips`]],template:function(a,o){a&1&&(Oe(),rt(0,`div`,0),ue(1),ut())},styles:[`.mat-mdc-chip-set {
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
`],encapsulation:2})})()}return t})();var Qu=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({providers:[Gr,{provide:_O,useValue:{separatorKeyCodes:[13]}}],imports:[ki,we]})}return t})();var KS=(()=>{class t{labelPosition=`after`;static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){return B({type:t,selectors:[[``,`mat-internal-form-field`,``]],hostAttrs:[1,`mdc-form-field`,`mat-internal-form-field`],hostVars:2,hostBindings:function(a,o){a&2&&W(`mdc-form-field--align-end`,o.labelPosition===`before`)},inputs:{labelPosition:`labelPosition`},ngContentSelectors:[`*`],decls:1,vars:0,template:function(a,o){a&1&&(Oe(),ue(0))},styles:[`.mat-internal-form-field {
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
`],encapsulation:2})})()}return t})();var ib={color:`accent`,clickAction:`check-indeterminate`,disabledInteractive:!1};var CO=new D(`mat-checkbox-default-options`,{providedIn:`root`,factory:()=>ib});var Jt=(function(t){return t[t.Init=0]=`Init`,t[t.Checked=1]=`Checked`,t[t.Unchecked=2]=`Unchecked`,t[t.Indeterminate=3]=`Indeterminate`,t})(Jt||{});var ab=class{source;checked};var Vc=(()=>{class t{_elementRef=f(G);_changeDetectorRef=f(Re);_ngZone=f(K);_animationsDisabled=ze();_options=f(CO,{optional:!0});focus(){this._inputElement.nativeElement.focus()}_createChangeEvent(e){let i=new ab;return i.source=this,i.checked=e,i}_getAnimationTargetElement(){return this._inputElement?.nativeElement}_animationClasses={uncheckedToChecked:`mdc-checkbox--anim-unchecked-checked`,uncheckedToIndeterminate:`mdc-checkbox--anim-unchecked-indeterminate`,checkedToUnchecked:`mdc-checkbox--anim-checked-unchecked`,checkedToIndeterminate:`mdc-checkbox--anim-checked-indeterminate`,indeterminateToChecked:`mdc-checkbox--anim-indeterminate-checked`,indeterminateToUnchecked:`mdc-checkbox--anim-indeterminate-unchecked`};ariaLabel=``;ariaLabelledby=null;ariaDescribedby;ariaExpanded;ariaControls;ariaOwns;_uniqueId;id;get inputId(){return`${this.id||this._uniqueId}-input`}required=!1;labelPosition=`after`;name=null;change=new J;indeterminateChange=new J;value;disableRipple=!1;_inputElement;tabIndex;color;disabledInteractive;_onTouched=()=>{};_currentAnimationClass=``;_currentCheckState=Jt.Init;_controlValueAccessorChangeFn=()=>{};_validatorChangeFn=()=>{};constructor(){f(Ze).load(cn);let e=f(new Yi(`tabindex`),{optional:!0});this._options=this._options||ib,this.color=this._options.color||ib.color,this.tabIndex=e==null?0:parseInt(e)||0,this.id=this._uniqueId=f(Le).getId(`mat-mdc-checkbox-`),this.disabledInteractive=this._options?.disabledInteractive??!1}ngOnChanges(e){e.required&&this._validatorChangeFn()}ngAfterViewInit(){this._syncIndeterminate(this.indeterminate)}get checked(){return this._checked}set checked(e){e!=this.checked&&(this._checked=e,this._changeDetectorRef.markForCheck())}_checked=!1;get disabled(){return this._disabled}set disabled(e){e!==this.disabled&&(this._disabled=e,this._changeDetectorRef.markForCheck())}_disabled=!1;get indeterminate(){return this._indeterminate()}set indeterminate(e){let i=e!=this._indeterminate();this._indeterminate.set(e),i&&(e?this._transitionCheckState(Jt.Indeterminate):this._transitionCheckState(this.checked?Jt.Checked:Jt.Unchecked),this.indeterminateChange.emit(e)),this._syncIndeterminate(e)}_indeterminate=M(!1);_isRippleDisabled(){return this.disableRipple||this.disabled}_onLabelTextChange(){this._changeDetectorRef.detectChanges()}writeValue(e){this.checked=!!e}registerOnChange(e){this._controlValueAccessorChangeFn=e}registerOnTouched(e){this._onTouched=e}setDisabledState(e){this.disabled=e}validate(e){return this.required&&e.value!==!0?{required:!0}:null}registerOnValidatorChange(e){this._validatorChangeFn=e}_transitionCheckState(e){let i=this._currentCheckState,a=this._getAnimationTargetElement();if(!(i===e||!a)&&(this._currentAnimationClass&&a.classList.remove(this._currentAnimationClass),this._currentAnimationClass=this._getAnimationClassForCheckStateTransition(i,e),this._currentCheckState=e,this._currentAnimationClass.length>0)){a.classList.add(this._currentAnimationClass);let o=this._currentAnimationClass;this._ngZone.runOutsideAngular(()=>{setTimeout(()=>{a.classList.remove(o)},1e3)})}}_emitChangeEvent(){this._controlValueAccessorChangeFn(this.checked),this.change.emit(this._createChangeEvent(this.checked)),this._inputElement&&(this._inputElement.nativeElement.checked=this.checked)}toggle(){this.checked=!this.checked,this._controlValueAccessorChangeFn(this.checked)}_handleInputClick(){let e=this._options?.clickAction;!this.disabled&&e!==`noop`?(this.indeterminate&&e!==`check`&&Promise.resolve().then(()=>{this._indeterminate.set(!1),this.indeterminateChange.emit(!1)}),this._checked=!this._checked,this._transitionCheckState(this._checked?Jt.Checked:Jt.Unchecked),this._emitChangeEvent()):(this.disabled&&this.disabledInteractive||!this.disabled&&e===`noop`)&&(this._inputElement.nativeElement.checked=this.checked,this._inputElement.nativeElement.indeterminate=this.indeterminate)}_onInteractionEvent(e){e.stopPropagation()}_onBlur(){Promise.resolve().then(()=>{this._onTouched(),this._changeDetectorRef.markForCheck()})}_getAnimationClassForCheckStateTransition(e,i){if(this._animationsDisabled)return``;switch(e){case Jt.Init:if(i===Jt.Checked)return this._animationClasses.uncheckedToChecked;if(i==Jt.Indeterminate)return this._checked?this._animationClasses.checkedToIndeterminate:this._animationClasses.uncheckedToIndeterminate;break;case Jt.Unchecked:return i===Jt.Checked?this._animationClasses.uncheckedToChecked:this._animationClasses.uncheckedToIndeterminate;case Jt.Checked:return i===Jt.Unchecked?this._animationClasses.checkedToUnchecked:this._animationClasses.checkedToIndeterminate;case Jt.Indeterminate:return i===Jt.Checked?this._animationClasses.indeterminateToChecked:this._animationClasses.indeterminateToUnchecked}return``}_syncIndeterminate(e){let i=this._inputElement;i&&(i.nativeElement.indeterminate=e)}_onInputClick(){this._handleInputClick()}_preventBubblingFromLabel(e){e.target&&this._inputElement&&e.target!==this._inputElement.nativeElement&&e.stopPropagation()}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`input`];return B({type:t,selectors:[[`mat-checkbox`]],viewQuery:function(o,r){if(o&1&&Xe(e,5),o&2){let s;X(s=Z())&&(r._inputElement=s.first)}},hostAttrs:[1,`mat-mdc-checkbox`],hostVars:16,hostBindings:function(o,r){o&2&&(Wt(`id`,r.id),ee(`tabindex`,null)(`aria-label`,null)(`aria-labelledby`,null),et(r.color?`mat-`+r.color:`mat-accent`),W(`_mat-animation-noopable`,r._animationsDisabled)(`mdc-checkbox--disabled`,r.disabled)(`mat-mdc-checkbox-disabled`,r.disabled)(`mat-mdc-checkbox-checked`,r.checked)(`mat-mdc-checkbox-disabled-interactive`,r.disabledInteractive))},inputs:{ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],ariaDescribedby:[0,`aria-describedby`,`ariaDescribedby`],ariaExpanded:[2,`aria-expanded`,`ariaExpanded`,V],ariaControls:[0,`aria-controls`,`ariaControls`],ariaOwns:[0,`aria-owns`,`ariaOwns`],id:`id`,required:[2,`required`,`required`,V],labelPosition:`labelPosition`,name:`name`,value:`value`,disableRipple:[2,`disableRipple`,`disableRipple`,V],tabIndex:[2,`tabIndex`,`tabIndex`,a=>a==null?void 0:st(a)],color:`color`,disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,V],checked:[2,`checked`,`checked`,V],disabled:[2,`disabled`,`disabled`,V],indeterminate:[2,`indeterminate`,`indeterminate`,V]},outputs:{change:`change`,indeterminateChange:`indeterminateChange`},exportAs:[`matCheckbox`],features:[$e([{provide:Ur,useExisting:zt(()=>t),multi:!0},{provide:$r,useExisting:t,multi:!0}]),ft],ngContentSelectors:[`*`],decls:15,vars:23,consts:[[`checkbox`,``],[`input`,``],[`label`,``],[`mat-internal-form-field`,``,3,`click`,`labelPosition`,`for`],[1,`mdc-checkbox`],[`aria-hidden`,`true`,1,`mat-mdc-checkbox-touch-target`],[`type`,`checkbox`,1,`mdc-checkbox__native-control`,3,`blur`,`click`,`change`,`checked`,`indeterminate`,`disabled`,`id`,`required`,`tabIndex`],[`aria-hidden`,`true`,1,`mdc-checkbox__ripple`],[`aria-hidden`,`true`,1,`mdc-checkbox__background`],[`focusable`,`false`,`viewBox`,`0 0 24 24`,1,`mdc-checkbox__checkmark`],[`fill`,`none`,`d`,`M1.73,12.91 8.1,19.28 22.79,4.59`,1,`mdc-checkbox__checkmark-path`],[1,`mdc-checkbox__mixedmark`],[`mat-ripple`,``,`aria-hidden`,`true`,1,`mat-mdc-checkbox-ripple`,`mat-focus-indicator`,3,`matRippleTrigger`,`matRippleDisabled`,`matRippleCentered`],[1,`mat-internal-form-field-label`,`mdc-label`]],template:function(o,r){if(o&1&&(Oe(),l(0,`label`,3),C(`click`,function(c){return r._preventBubblingFromLabel(c)}),l(1,`span`,4,0),z(3,`span`,5),l(4,`input`,6,1),C(`blur`,function(){return r._onBlur()})(`click`,function(){return r._onInputClick()})(`change`,function(c){return r._onInteractionEvent(c)}),d(),z(6,`span`,7),l(7,`span`,8),$t(),l(8,`svg`,9),z(9,`path`,10),d(),va(),z(10,`span`,11),d(),z(11,`span`,12),d(),l(12,`span`,13,2),ue(14),d()()),o&2){let s=mt(2);S(`labelPosition`,r.labelPosition)(`for`,r.inputId),h(4),W(`mdc-checkbox--selected`,r.checked),S(`checked`,r.checked)(`indeterminate`,r.indeterminate)(`disabled`,r.disabled&&!r.disabledInteractive)(`id`,r.inputId)(`required`,r.required)(`tabIndex`,r.disabled&&!r.disabledInteractive?-1:r.tabIndex),ee(`aria-label`,r.ariaLabel||null)(`aria-labelledby`,r.ariaLabelledby)(`aria-describedby`,r.ariaDescribedby)(`aria-checked`,r.indeterminate?`mixed`:null)(`aria-controls`,r.ariaControls)(`aria-disabled`,r.disabled&&r.disabledInteractive?!0:null)(`aria-expanded`,r.ariaExpanded)(`aria-owns`,r.ariaOwns)(`name`,r.name)(`value`,r.value),h(7),S(`matRippleTrigger`,s)(`matRippleDisabled`,r.disableRipple||r.disabled)(`matRippleCentered`,!0)}},dependencies:[ci,KS],styles:[`.mdc-checkbox {
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
`],encapsulation:2})})()}return t})();var Xu=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({imports:[Vc,we]})}return t})();var di=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({imports:[Cu,Mi,we]})}return t})();var SO=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=B({type:t,selectors:[[`ng-component`]],hostAttrs:[`cdk-text-field-style-loader`,``],decls:0,vars:0,template:function(i,a){},styles:[`textarea.cdk-textarea-autosize {
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
`],encapsulation:2})}return t})();var wO={passive:!0};var XS=(()=>{class t{_platform=f(Te);_ngZone=f(K);_renderer=f(kt).createRenderer(null,null);_styleLoader=f(Ze);_monitoredElements=new Map;monitor(e){if(!this._platform.isBrowser)return Oi;this._styleLoader.load(SO);let i=sn(e),a=this._monitoredElements.get(i);if(a)return a.subject;let o=new L,r=`cdk-text-field-autofilled`,s=m=>{m.animationName===`cdk-text-field-autofill-start`&&!i.classList.contains(r)?(i.classList.add(r),this._ngZone.run(()=>o.next({target:m.target,isAutofilled:!0}))):m.animationName===`cdk-text-field-autofill-end`&&i.classList.contains(r)&&(i.classList.remove(r),this._ngZone.run(()=>o.next({target:m.target,isAutofilled:!1})))},c=this._ngZone.runOutsideAngular(()=>(i.classList.add(`cdk-text-field-autofill-monitored`),this._renderer.listen(i,`animationstart`,s,wO)));return this._monitoredElements.set(i,{subject:o,unlisten:c}),o}stopMonitoring(e){let i=sn(e),a=this._monitoredElements.get(i);a&&(a.unlisten(),a.subject.complete(),i.classList.remove(`cdk-text-field-autofill-monitored`),i.classList.remove(`cdk-text-field-autofilled`),this._monitoredElements.delete(i))}ngOnDestroy(){this._monitoredElements.forEach((e,i)=>this.stopMonitoring(i))}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var ZS=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({})}return t})();var JS=new D(`MAT_INPUT_VALUE_ACCESSOR`);var EO=[`button`,`checkbox`,`file`,`hidden`,`image`,`radio`,`range`,`reset`,`submit`];var DO=new D(`MAT_INPUT_CONFIG`);var qr=(()=>{class t{_elementRef=f(G);_platform=f(Te);ngControl=f(Ii,{optional:!0,self:!0});_autofillMonitor=f(XS);_ngZone=f(K);_formField=f(Fc,{optional:!0});_renderer=f(Ae);_uid=f(Le).getId(`mat-input-`);_previousNativeValue;_inputValueAccessor;_signalBasedValueAccessor;_previousPlaceholder=null;_errorStateTracker;_config=f(DO,{optional:!0});_cleanupIosKeyup;_cleanupWebkitWheel;_isServer=!1;_isNativeSelect=!1;_isTextarea=!1;_isInFormField=!1;focused=!1;stateChanges=new L;controlType=`mat-input`;autofilled=!1;get disabled(){return this._disabled}set disabled(e){this._disabled=ea(e),this.focused&&(this.focused=!1,this.stateChanges.next())}_disabled=!1;get id(){return this._id}set id(e){this._id=e||this._uid}_id;placeholder;name;get required(){return this._required??this.ngControl?.control?.hasValidator(Ao.required)??!1}set required(e){this._required=ea(e)}_required;get type(){return this._type}set type(e){this._type=e||`text`,this._validateType(),!this._isTextarea&&Lg().has(this._type)&&(this._elementRef.nativeElement.type=this._type)}_type=`text`;get errorStateMatcher(){return this._errorStateTracker.matcher}set errorStateMatcher(e){this._errorStateTracker.matcher=e}userAriaDescribedBy;get value(){return this._signalBasedValueAccessor?this._signalBasedValueAccessor.value():this._inputValueAccessor.value}set value(e){e!==this.value&&(this._signalBasedValueAccessor?this._signalBasedValueAccessor.value.set(e):this._inputValueAccessor.value=e,this.stateChanges.next())}get readonly(){return this._readonly}set readonly(e){this._readonly=ea(e)}_readonly=!1;disabledInteractive;get errorState(){return this._errorStateTracker.errorState}set errorState(e){this._errorStateTracker.errorState=e}_neverEmptyInputTypes=[`date`,`datetime`,`datetime-local`,`month`,`time`,`week`].filter(e=>Lg().has(e));constructor(){let e=f(Ac,{optional:!0}),i=f(Oc,{optional:!0}),a=f(Gr),o=f(JS,{optional:!0,self:!0}),r=f(Wu,{optional:!0,self:!0}),s=this._elementRef.nativeElement,c=s.nodeName.toLowerCase();o?on(o.value)?this._signalBasedValueAccessor=o:this._inputValueAccessor=o:this._inputValueAccessor=s,this._previousNativeValue=this.value,this.id=this.id,this._platform.IOS&&this._ngZone.runOutsideAngular(()=>{this._cleanupIosKeyup=this._renderer.listen(s,`keyup`,this._iOSKeyupListener)}),this._errorStateTracker=new Wr(a,r||this.ngControl,i,e,this.stateChanges),this._isServer=!this._platform.isBrowser,this._isNativeSelect=c===`select`,this._isTextarea=c===`textarea`,this._isInFormField=!!this._formField,this.disabledInteractive=this._config?.disabledInteractive||!1,this._isNativeSelect&&(this.controlType=s.multiple?`mat-native-select-multiple`:`mat-native-select`),this._signalBasedValueAccessor&&Pt(()=>{this._signalBasedValueAccessor.value(),this.stateChanges.next()})}ngAfterViewInit(){this._platform.isBrowser&&this._autofillMonitor.monitor(this._elementRef.nativeElement).subscribe(e=>{this.autofilled=e.isAutofilled,this.stateChanges.next()})}ngOnChanges(){this.stateChanges.next()}ngOnDestroy(){this.stateChanges.complete(),this._platform.isBrowser&&this._autofillMonitor.stopMonitoring(this._elementRef.nativeElement),this._cleanupIosKeyup?.(),this._cleanupWebkitWheel?.()}ngDoCheck(){this.ngControl&&(this.updateErrorState(),this.ngControl.disabled!==null&&this.ngControl.disabled!==this.disabled&&(this.disabled=this.ngControl.disabled,this.stateChanges.next())),this._dirtyCheckNativeValue(),this._dirtyCheckPlaceholder()}focus(e){this._elementRef.nativeElement.focus(e)}updateErrorState(){this._errorStateTracker.updateErrorState()}_focusChanged(e){if(e!==this.focused){if(!this._isNativeSelect&&e&&this.disabled&&this.disabledInteractive){let i=this._elementRef.nativeElement;i.type===`number`?(i.type=`text`,i.setSelectionRange(0,0),i.type=`number`):i.setSelectionRange(0,0)}this.focused=e,this.stateChanges.next()}}_onInput(){}_dirtyCheckNativeValue(){let e=this._elementRef.nativeElement.value;this._previousNativeValue!==e&&(this._previousNativeValue=e,this.stateChanges.next())}_dirtyCheckPlaceholder(){let e=this._getPlaceholder();if(e!==this._previousPlaceholder){let i=this._elementRef.nativeElement;this._previousPlaceholder=e,e?i.setAttribute(`placeholder`,e):i.removeAttribute(`placeholder`)}}_getPlaceholder(){return this.placeholder||null}_validateType(){EO.indexOf(this._type)}_isNeverEmpty(){return this._neverEmptyInputTypes.indexOf(this._type)>-1}_isBadInput(){let e=this._elementRef.nativeElement.validity;return e&&e.badInput}get empty(){return!this._isNeverEmpty()&&!this._elementRef.nativeElement.value&&!this._isBadInput()&&!this.autofilled}get shouldLabelFloat(){if(this._isNativeSelect){let e=this._elementRef.nativeElement,i=e.options[0];return this.focused||e.multiple||!this.empty||!!(e.selectedIndex>-1&&i&&i.label)}else return this.focused&&!this.disabled||!this.empty}get describedByIds(){return this._elementRef.nativeElement.getAttribute(`aria-describedby`)?.split(` `)||[]}setDescribedByIds(e){let i=this._elementRef.nativeElement;e.length?i.setAttribute(`aria-describedby`,e.join(` `)):i.removeAttribute(`aria-describedby`)}onContainerClick(){this.focused||this.focus()}_isInlineSelect(){let e=this._elementRef.nativeElement;return this._isNativeSelect&&(e.multiple||e.size>1)}_iOSKeyupListener=e=>{let i=e.target;!i.value&&i.selectionStart===0&&i.selectionEnd===0&&(i.setSelectionRange(1,1),i.setSelectionRange(0,0))};_getReadonlyAttribute(){return this._isNativeSelect?null:this.readonly||this.disabled&&this.disabledInteractive?`true`:null}static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,selectors:[[`input`,`matInput`,``],[`textarea`,`matInput`,``],[`select`,`matNativeControl`,``],[`input`,`matNativeControl`,``],[`textarea`,`matNativeControl`,``]],hostAttrs:[1,`mat-mdc-input-element`],hostVars:21,hostBindings:function(i,a){i&1&&C(`focus`,function(){return a._focusChanged(!0)})(`blur`,function(){return a._focusChanged(!1)})(`input`,function(){return a._onInput()}),i&2&&(Wt(`id`,a.id)(`disabled`,a.disabled&&!a.disabledInteractive)(`required`,a.required),ee(`name`,a.name||null)(`readonly`,a._getReadonlyAttribute())(`aria-disabled`,a.disabled&&a.disabledInteractive?`true`:null)(`aria-invalid`,a.empty&&a.required?null:a.errorState)(`aria-required`,a.required)(`id`,a.id),W(`mat-input-server`,a._isServer)(`mat-mdc-form-field-textarea-control`,a._isInFormField&&a._isTextarea)(`mat-mdc-form-field-input-control`,a._isInFormField)(`mat-mdc-input-disabled-interactive`,a.disabledInteractive)(`mdc-text-field__input`,a._isInFormField)(`mat-mdc-native-select-inline`,a._isInlineSelect()))},inputs:{disabled:`disabled`,id:`id`,placeholder:`placeholder`,name:`name`,required:`required`,type:`type`,errorStateMatcher:`errorStateMatcher`,userAriaDescribedBy:[0,`aria-describedby`,`userAriaDescribedBy`],value:`value`,readonly:`readonly`,disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,V]},exportAs:[`matInput`],features:[$e([{provide:Pc,useExisting:t}]),ft]})}return t})();var Yr=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({imports:[di,di,ZS,we]})}return t})();var Bc=class{_attachedHost=null;attach(n){return this._attachedHost=n,n.attach(this)}detach(){let n=this._attachedHost;n!=null&&(this._attachedHost=null,n.detach())}get isAttached(){return this._attachedHost!=null}setAttachedHost(n){this._attachedHost=n}};var Na=class extends Bc{component;viewContainerRef;injector;projectableNodes;bindings;directives;constructor(n,e,i,a,o,r){super(),this.component=n,this.viewContainerRef=e,this.injector=i,this.projectableNodes=a,this.bindings=o||null,this.directives=r||null}};var zn=class extends Bc{templateRef;viewContainerRef;context;injector;constructor(n,e,i,a){super(),this.templateRef=n,this.viewContainerRef=e,this.context=i,this.injector=a}get origin(){return this.templateRef.elementRef}attach(n,e=this.context){return this.context=e,super.attach(n)}detach(){return this.context=void 0,super.detach()}};var sb=class extends Bc{element;constructor(n){super(),this.element=n instanceof G?n.nativeElement:n}};var Kr=class{_attachedPortal=null;_disposeFn=null;_isDisposed=!1;hasAttached(){return!!this._attachedPortal}attach(n){if(n instanceof Na)return this._attachedPortal=n,this.attachComponentPortal(n);if(n instanceof zn)return this._attachedPortal=n,this.attachTemplatePortal(n);if(this.attachDomPortal&&n instanceof sb)return this._attachedPortal=n,this.attachDomPortal(n)}attachDomPortal=null;detach(){this._attachedPortal&&(this._attachedPortal.setAttachedHost(null),this._attachedPortal=null),this._invokeDisposeFn()}dispose(){this.hasAttached()&&this.detach(),this._invokeDisposeFn(),this._isDisposed=!0}setDisposeFn(n){this._disposeFn=n}_invokeDisposeFn(){this._disposeFn&&(this._disposeFn(),this._disposeFn=null)}};var jc=class extends Kr{outletElement;_appRef;_defaultInjector;constructor(n,e,i){super(),this.outletElement=n,this._appRef=e,this._defaultInjector=i}attachComponentPortal(n){let e;if(n.viewContainerRef){let i=n.injector||n.viewContainerRef.injector,a=i.get(ti,null,{optional:!0})||void 0;e=n.viewContainerRef.createComponent(n.component,{index:n.viewContainerRef.length,injector:i,ngModuleRef:a,projectableNodes:n.projectableNodes||void 0,bindings:n.bindings||void 0,directives:n.directives||void 0}),this.setDisposeFn(()=>e.destroy())}else{let i=this._appRef,a=n.injector||this._defaultInjector||re.NULL,o=a.get(Et,i.injector);e=Xd(n.component,{elementInjector:a,environmentInjector:o,projectableNodes:n.projectableNodes||void 0,bindings:n.bindings||void 0,directives:n.directives||void 0}),i.attachView(e.hostView),this.setDisposeFn(()=>{i.viewCount>0&&i.detachView(e.hostView),e.destroy()})}return this.outletElement.appendChild(this._getComponentRootNode(e)),this._attachedPortal=n,e}attachTemplatePortal(n){let e=n.viewContainerRef,i=e.createEmbeddedView(n.templateRef,n.context,{injector:n.injector});return i.rootNodes.forEach(a=>this.outletElement.appendChild(a)),i.detectChanges(),this.setDisposeFn(()=>{let a=e.indexOf(i);a!==-1&&e.remove(a)}),this._attachedPortal=n,i}attachDomPortal=n=>{let e=n.element;e.parentNode;let i=this.outletElement.ownerDocument.createComment(`dom-portal`);e.parentNode.insertBefore(i,e),this.outletElement.appendChild(e),this._attachedPortal=n,super.setDisposeFn(()=>{i.parentNode&&i.parentNode.replaceChild(e,i)})};dispose(){super.dispose(),this.outletElement.remove()}_getComponentRootNode(n){return n.hostView.rootNodes[0]}};var ew=(()=>{class t extends zn{constructor(){let e=f(Ft),i=f(Lt);super(e,i)}static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,selectors:[[``,`cdkPortal`,``]],exportAs:[`cdkPortal`],features:[Fe]})}return t})();var Ta=(()=>{class t extends Kr{_moduleRef=f(ti,{optional:!0});_document=f(ce);_viewContainerRef=f(Lt);_isInitialized=!1;_attachedRef=null;get portal(){return this._attachedPortal}set portal(e){this.hasAttached()&&!e&&!this._isInitialized||(this.hasAttached()&&super.detach(),e&&super.attach(e),this._attachedPortal=e||null)}attached=new J;get attachedRef(){return this._attachedRef}ngOnInit(){this._isInitialized=!0}ngOnDestroy(){super.dispose(),this._attachedRef=this._attachedPortal=null}attachComponentPortal(e){e.setAttachedHost(this);let i=e.viewContainerRef!=null?e.viewContainerRef:this._viewContainerRef,a=i.createComponent(e.component,{index:i.length,injector:e.injector||i.injector,projectableNodes:e.projectableNodes||void 0,ngModuleRef:this._moduleRef||void 0,bindings:e.bindings||void 0,directives:e.directives||void 0});return i!==this._viewContainerRef&&this._getRootNode().appendChild(a.hostView.rootNodes[0]),super.setDisposeFn(()=>a.destroy()),this._attachedPortal=e,this._attachedRef=a,this.attached.emit(a),a}attachTemplatePortal(e){e.setAttachedHost(this);let i=this._viewContainerRef.createEmbeddedView(e.templateRef,e.context,{injector:e.injector});return super.setDisposeFn(()=>this._viewContainerRef.clear()),this._attachedPortal=e,this._attachedRef=i,this.attached.emit(i),i}attachDomPortal=e=>{let i=e.element;i.parentNode;let a=this._document.createComment(`dom-portal`);e.setAttachedHost(this),i.parentNode.insertBefore(a,i),this._getRootNode().appendChild(i),this._attachedPortal=e,super.setDisposeFn(()=>{a.parentNode&&a.parentNode.replaceChild(i,a)})};_getRootNode(){let e=this._viewContainerRef.element.nativeElement;return e.nodeType===e.ELEMENT_NODE?e:e.parentNode}static ɵfac=(()=>{let e;return function(a){return(e||(e=It(t)))(a||t)}})();static ɵdir=q({type:t,selectors:[[``,`cdkPortalOutlet`,``]],inputs:{portal:[0,`cdkPortalOutlet`,`portal`]},outputs:{attached:`attached`},exportAs:[`cdkPortalOutlet`],features:[Fe]})}return t})();var Ro=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({})}return t})();var kO=20;var Aa=(()=>{class t{_ngZone=f(K);_platform=f(Te);_renderer=f(kt).createRenderer(null,null);_cleanupGlobalListener;_scrolled=new L;_scrolledCount=0;scrollContainers=new Map;register(e){this.scrollContainers.has(e)||this.scrollContainers.set(e,e.elementScrolled().subscribe(()=>this._scrolled.next(e)))}deregister(e){let i=this.scrollContainers.get(e);i&&(i.unsubscribe(),this.scrollContainers.delete(e))}scrolled(e=kO){return this._platform.isBrowser?new xe(i=>{this._cleanupGlobalListener||(this._cleanupGlobalListener=this._ngZone.runOutsideAngular(()=>this._renderer.listen(`document`,`scroll`,()=>this._scrolled.next())));let a=e>0?this._scrolled.pipe(Tl(e)).subscribe(i):this._scrolled.subscribe(i);return this._scrolledCount++,()=>{a.unsubscribe(),this._scrolledCount--,this._scrolledCount||(this._cleanupGlobalListener?.(),this._cleanupGlobalListener=void 0)}}):In()}ngOnDestroy(){this._cleanupGlobalListener?.(),this._cleanupGlobalListener=void 0,this.scrollContainers.forEach((e,i)=>this.deregister(i)),this._scrolled.complete()}ancestorScrolled(e,i){let a=this.getAncestorScrollContainers(e);return this.scrolled(i).pipe(Ke(o=>!o||a.indexOf(o)>-1))}getAncestorScrollContainers(e){let i=[];return this.scrollContainers.forEach((a,o)=>{this._targetContainsElement(o,e)&&i.push(o)}),i}_targetContainsElement(e,i){let a=sn(i),o=e.getElementRef().nativeElement;do if(a==o)return!0;while(a=a.parentElement);return!1}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var zc=(()=>{class t{elementRef=f(G);scrollDispatcher=f(Aa);ngZone=f(K);dir=f(pt,{optional:!0});_scrollElement=this.elementRef.nativeElement;_destroyed=new L;_renderer=f(Ae);_cleanupScroll;_elementScrolled=new L;ngOnInit(){this._cleanupScroll=this.ngZone.runOutsideAngular(()=>this._renderer.listen(this._scrollElement,`scroll`,e=>this._elementScrolled.next(e))),this.scrollDispatcher.register(this)}ngOnDestroy(){this._cleanupScroll?.(),this._elementScrolled.complete(),this.scrollDispatcher.deregister(this),this._destroyed.next(),this._destroyed.complete()}elementScrolled(){return this._elementScrolled}getElementRef(){return this.elementRef}scrollTo(e){let i=this.elementRef.nativeElement,a=this.dir&&this.dir.value==`rtl`;e.left??=a?e.end:e.start,e.right??=a?e.start:e.end,e.bottom!=null&&(e.top=i.scrollHeight-i.clientHeight-e.bottom),a&&Fr()!=si.NORMAL?(e.left!=null&&(e.right=i.scrollWidth-i.clientWidth-e.left),Fr()==si.INVERTED?e.left=e.right:Fr()==si.NEGATED&&(e.left=e.right?-e.right:e.right)):e.right!=null&&(e.left=i.scrollWidth-i.clientWidth-e.right),this._applyScrollToOptions(e)}_applyScrollToOptions(e){let i=this.elementRef.nativeElement;Mu()?i.scrollTo(e):(e.top!=null&&(i.scrollTop=e.top),e.left!=null&&(i.scrollLeft=e.left))}measureScrollOffset(e){let i=`left`,a=`right`,o=this.elementRef.nativeElement;if(e==`top`)return o.scrollTop;if(e==`bottom`)return o.scrollHeight-o.clientHeight-o.scrollTop;let r=this.dir&&this.dir.value==`rtl`;return e==`start`?e=r?a:i:e==`end`&&(e=r?i:a),r&&Fr()==si.INVERTED?e==i?o.scrollWidth-o.clientWidth-o.scrollLeft:o.scrollLeft:r&&Fr()==si.NEGATED?e==i?o.scrollLeft+o.scrollWidth-o.clientWidth:-o.scrollLeft:e==i?o.scrollLeft:o.scrollWidth-o.clientWidth-o.scrollLeft}static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,selectors:[[``,`cdk-scrollable`,``],[``,`cdkScrollable`,``]]})}return t})();var IO=20;var ui=(()=>{class t{_platform=f(Te);_listeners;_viewportSize=null;_change=new L;_document=f(ce);constructor(){let e=f(K),i=f(kt).createRenderer(null,null);e.runOutsideAngular(()=>{if(this._platform.isBrowser){let a=o=>this._change.next(o);this._listeners=[i.listen(`window`,`resize`,a),i.listen(`window`,`orientationchange`,a)]}this.change().subscribe(()=>this._viewportSize=null)})}ngOnDestroy(){this._listeners?.forEach(e=>e()),this._change.complete()}getViewportSize(){this._viewportSize||this._updateViewportSize();let e={width:this._viewportSize.width,height:this._viewportSize.height};return this._platform.isBrowser||(this._viewportSize=null),e}getViewportRect(){let e=this.getViewportScrollPosition(),{width:i,height:a}=this.getViewportSize();return{top:e.top,left:e.left,bottom:e.top+a,right:e.left+i,height:a,width:i}}getViewportScrollPosition(){if(!this._platform.isBrowser)return{top:0,left:0};let e=this._document,i=this._getWindow(),a=e.documentElement,o=a.getBoundingClientRect();return{top:-o.top||e.body?.scrollTop||i.scrollY||a.scrollTop||0,left:-o.left||e.body?.scrollLeft||i.scrollX||a.scrollLeft||0}}change(e=IO){return e>0?this._change.pipe(Tl(e)):this._change}_getWindow(){return this._document.defaultView||window}_updateViewportSize(){let e=this._getWindow();this._viewportSize=this._platform.isBrowser?{width:e.innerWidth,height:e.innerHeight}:{width:0,height:0}}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var na=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({})}return t})();var cb=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({imports:[we,na,we,na]})}return t})();var tw=Mu();function Zr(t){return new Zu(t.get(ui),t.get(ce))}var Zu=class{_viewportRuler;_previousHTMLStyles={top:``,left:``};_previousScrollPosition;_isEnabled=!1;_document;constructor(n,e){this._viewportRuler=n,this._document=e}attach(){}enable(){if(this._canBeEnabled()){let n=this._document.documentElement;this._previousScrollPosition=this._viewportRuler.getViewportScrollPosition(),this._previousHTMLStyles.left=n.style.left||``,this._previousHTMLStyles.top=n.style.top||``,n.style.left=St(-this._previousScrollPosition.left),n.style.top=St(-this._previousScrollPosition.top),n.classList.add(`cdk-global-scrollblock`),this._isEnabled=!0}}disable(){if(this._isEnabled){let n=this._document.documentElement,e=this._document.body,i=n.style,a=e.style,o=i.scrollBehavior||``,r=a.scrollBehavior||``;this._isEnabled=!1,i.left=this._previousHTMLStyles.left,i.top=this._previousHTMLStyles.top,n.classList.remove(`cdk-global-scrollblock`),tw&&(i.scrollBehavior=a.scrollBehavior=`auto`),window.scroll(this._previousScrollPosition.left,this._previousScrollPosition.top),tw&&(i.scrollBehavior=o,a.scrollBehavior=r)}}_canBeEnabled(){if(this._document.documentElement.classList.contains(`cdk-global-scrollblock`)||this._isEnabled)return!1;let e=this._document.documentElement,i=this._viewportRuler.getViewportSize();return e.scrollHeight>i.height||e.scrollWidth>i.width}};function cw(t,n){return new Ju(t.get(Aa),t.get(K),t.get(ui),n)}var Ju=class{_scrollDispatcher;_ngZone;_viewportRuler;_config;_scrollSubscription=null;_overlayRef;_initialScrollPosition;constructor(n,e,i,a){this._scrollDispatcher=n,this._ngZone=e,this._viewportRuler=i,this._config=a}attach(n){this._overlayRef,this._overlayRef=n}enable(){if(this._scrollSubscription)return;let n=this._scrollDispatcher.scrolled(0).pipe(Ke(e=>!e||!this._overlayRef.overlayElement.contains(e.getElementRef().nativeElement)));this._config&&this._config.threshold&&this._config.threshold>1?(this._initialScrollPosition=this._viewportRuler.getViewportScrollPosition().top,this._scrollSubscription=n.subscribe(()=>{let e=this._viewportRuler.getViewportScrollPosition().top;Math.abs(e-this._initialScrollPosition)>this._config.threshold?this._detach():this._overlayRef.updatePosition()})):this._scrollSubscription=n.subscribe(this._detach)}disable(){this._scrollSubscription&&(this._scrollSubscription.unsubscribe(),this._scrollSubscription=null)}detach(){this.disable(),this._overlayRef=null}_detach=()=>{this.disable(),this._overlayRef.hasAttached()&&this._ngZone.run(()=>this._overlayRef.detach())}};var Hc=class{enable(){}disable(){}attach(){}};function db(t,n){return n.some(e=>{let i=t.bottom<e.top,a=t.top>e.bottom,o=t.right<e.left,r=t.left>e.right;return i||a||o||r})}function nw(t,n){return n.some(e=>{let i=t.top<e.top,a=t.bottom>e.bottom,o=t.left<e.left,r=t.right>e.right;return i||a||o||r})}function oa(t,n){return new em(t.get(Aa),t.get(ui),t.get(K),n)}var em=class{_scrollDispatcher;_viewportRuler;_ngZone;_config;_scrollSubscription=null;_overlayRef;constructor(n,e,i,a){this._scrollDispatcher=n,this._viewportRuler=e,this._ngZone=i,this._config=a}attach(n){this._overlayRef,this._overlayRef=n}enable(){if(!this._scrollSubscription){let n=this._config?this._config.scrollThrottle:0;this._scrollSubscription=this._scrollDispatcher.scrolled(n).subscribe(()=>{if(this._overlayRef.updatePosition(),this._config&&this._config.autoClose){let e=this._overlayRef.overlayElement.getBoundingClientRect(),{width:i,height:a}=this._viewportRuler.getViewportSize();db(e,[{width:i,height:a,bottom:a,right:i,top:0,left:0}])&&(this.disable(),this._ngZone.run(()=>this._overlayRef.detach()))}})}}disable(){this._scrollSubscription&&(this._scrollSubscription.unsubscribe(),this._scrollSubscription=null)}detach(){this.disable(),this._overlayRef=null}};var lw=(()=>{class t{_injector=f(re);noop=()=>new Hc;close=e=>cw(this._injector,e);block=()=>Zr(this._injector);reposition=e=>oa(this._injector,e);static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var ia=class{positionStrategy;scrollStrategy=new Hc;panelClass=``;hasBackdrop=!1;backdropClass=`cdk-overlay-dark-backdrop`;disableAnimations;width;height;minWidth;minHeight;maxWidth;maxHeight;direction;disposeOnNavigation=!1;usePopover;eventPredicate;constructor(n){if(n){let e=Object.keys(n);for(let i of e)n[i]!==void 0&&(this[i]=n[i])}}};var tm=class{connectionPair;scrollableViewProperties;constructor(n,e){this.connectionPair=n,this.scrollableViewProperties=e}};var dw=(()=>{class t{_attachedOverlays=[];_document=f(ce);_isAttached=!1;ngOnDestroy(){this.detach()}add(e){this.remove(e),this._attachedOverlays.push(e)}remove(e){let i=this._attachedOverlays.indexOf(e);i>-1&&this._attachedOverlays.splice(i,1),this._attachedOverlays.length===0&&this.detach()}canReceiveEvent(e,i,a){return a.observers.length<1?!1:e.eventPredicate?e.eventPredicate(i):!0}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var uw=(()=>{class t extends dw{_ngZone=f(K);_renderer=f(kt).createRenderer(null,null);_cleanupKeydown;add(e){super.add(e),this._isAttached||(this._ngZone.runOutsideAngular(()=>{this._cleanupKeydown=this._renderer.listen(`body`,`keydown`,this._keydownListener)}),this._isAttached=!0)}detach(){this._isAttached&&(this._cleanupKeydown?.(),this._isAttached=!1)}_keydownListener=e=>{let i=this._attachedOverlays;for(let a=i.length-1;a>-1;a--){let o=i[a];if(this.canReceiveEvent(o,e,o._keydownEvents)){this._ngZone.run(()=>o._keydownEvents.next(e));break}}};static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var mw=(()=>{class t extends dw{_platform=f(Te);_ngZone=f(K);_renderer=f(kt).createRenderer(null,null);_cursorOriginalValue;_cursorStyleIsSet=!1;_pointerDownEventTarget=null;_cleanups;add(e){if(super.add(e),!this._isAttached){let i=this._document.body,a={capture:!0},o=this._renderer;this._cleanups=this._ngZone.runOutsideAngular(()=>[o.listen(i,`pointerdown`,this._pointerDownListener,a),o.listen(i,`click`,this._clickListener,a),o.listen(i,`auxclick`,this._clickListener,a),o.listen(i,`contextmenu`,this._clickListener,a)]),this._platform.IOS&&!this._cursorStyleIsSet&&(this._cursorOriginalValue=i.style.cursor,i.style.cursor=`pointer`,this._cursorStyleIsSet=!0),this._isAttached=!0}}detach(){this._isAttached&&(this._cleanups?.forEach(e=>e()),this._cleanups=void 0,this._platform.IOS&&this._cursorStyleIsSet&&(this._document.body.style.cursor=this._cursorOriginalValue,this._cursorStyleIsSet=!1),this._isAttached=!1)}_pointerDownListener=e=>{this._pointerDownEventTarget=qt(e)};_clickListener=e=>{let i=qt(e),a=e.type===`click`&&this._pointerDownEventTarget?this._pointerDownEventTarget:i;this._pointerDownEventTarget=null;let o=this._attachedOverlays.slice();for(let r=o.length-1;r>-1;r--){let s=o[r],c=s._outsidePointerEvents;if(!(!s.hasAttached()||!this.canReceiveEvent(s,e,c))){if(iw(s.overlayElement,i)||iw(s.overlayElement,a))break;this._ngZone?this._ngZone.run(()=>c.next(e)):c.next(e)}}};static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();function iw(t,n){let e=typeof ShadowRoot<`u`&&ShadowRoot,i=n;for(;i;){if(i===t)return!0;i=e&&i instanceof ShadowRoot?i.host:i.parentNode}return!1}var pw=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=B({type:t,selectors:[[`ng-component`]],hostAttrs:[`cdk-overlay-style-loader`,``],decls:0,vars:0,template:function(i,a){},styles:[`.cdk-overlay-container, .cdk-global-overlay-wrapper {
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
`],encapsulation:2})}return t})();var am=(()=>{class t{_platform=f(Te);_containerElement;_document=f(ce);_styleLoader=f(Ze);ngOnDestroy(){this._containerElement?.remove()}getContainerElement(){return this._loadStyles(),this._containerElement||this._createContainer(),this._containerElement}_createContainer(){let e=`cdk-overlay-container`;if(this._platform.isBrowser||Fg()){let a=this._document.querySelectorAll(`.${e}[platform="server"], .${e}[platform="test"]`);for(let o=0;o<a.length;o++)a[o].remove()}let i=this._document.createElement(`div`);i.classList.add(e),Fg()?i.setAttribute(`platform`,`test`):this._platform.isBrowser||i.setAttribute(`platform`,`server`),this._document.body.appendChild(i),this._containerElement=i}_loadStyles(){this._styleLoader.load(pw)}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var ub=class{_renderer;_ngZone;element;_cleanupClick;_cleanupTransitionEnd;_fallbackTimeout;constructor(n,e,i,a){this._renderer=e,this._ngZone=i,this.element=n.createElement(`div`),this.element.classList.add(`cdk-overlay-backdrop`),this._cleanupClick=e.listen(this.element,`click`,a)}detach(){this._ngZone.runOutsideAngular(()=>{let n=this.element;clearTimeout(this._fallbackTimeout),this._cleanupTransitionEnd?.(),this._cleanupTransitionEnd=this._renderer.listen(n,`transitionend`,this.dispose),this._fallbackTimeout=setTimeout(this.dispose,500),n.style.pointerEvents=`none`,n.classList.remove(`cdk-overlay-backdrop-showing`)})}dispose=()=>{clearTimeout(this._fallbackTimeout),this._cleanupClick?.(),this._cleanupTransitionEnd?.(),this._cleanupClick=this._cleanupTransitionEnd=this._fallbackTimeout=void 0,this.element.remove()}};function mb(t){return t&&t.nodeType===1}var lb=M([]);var Qr=class{_portalOutlet;_host;_pane;_config;_ngZone;_keyboardDispatcher;_document;_location;_outsideClickDispatcher;_animationsDisabled;_injector;_renderer;_backdropClick=new L;_attachments=new L;_detachments=new L;_positionStrategy;_scrollStrategy;_locationChanges;_backdropRef=null;_detachContentMutationObserver;_detachContentAfterRenderRef;_disposed=!1;_previousHostParent;_keydownEvents=new L;_outsidePointerEvents=new L;_afterNextRenderRef;constructor(n,e,i,a,o,r,s,c,m,u=!1,g,v){this._portalOutlet=n,this._host=e,this._pane=i,this._config=a,this._ngZone=o,this._keyboardDispatcher=r,this._document=s,this._location=c,this._outsideClickDispatcher=m,this._animationsDisabled=u,this._injector=g,this._renderer=v,a.scrollStrategy&&(this._scrollStrategy=a.scrollStrategy,this._scrollStrategy.attach(this)),this._positionStrategy=a.positionStrategy}get overlayElement(){return this._pane}get backdropElement(){return this._backdropRef?.element||null}get hostElement(){return this._host}get eventPredicate(){return this._config?.eventPredicate||null}attach(n){if(this._disposed)return null;this._attachHost();let e=this._portalOutlet.attach(n);if(this._positionStrategy?.attach(this),this._updateStackingOrder(),this._updateElementSize(),this._updateElementDirection(),tt(()=>{lb.update(i=>i.includes(this)?i:[...i,this])}),this._scrollStrategy&&this._scrollStrategy.enable(),this._afterNextRenderRef?.destroy(),this._afterNextRenderRef=Mt(()=>{this.hasAttached()&&this.updatePosition()},{injector:this._injector}),this._togglePointerEvents(!0),this._config.hasBackdrop&&this._attachBackdrop(),this._config.panelClass&&this._toggleClasses(this._pane,this._config.panelClass,!0),this._attachments.next(),this._completeDetachContent(),this._keyboardDispatcher.add(this),this._config.disposeOnNavigation===!0||this._config.disposeOnNavigation===`pop-state`){let i=this._location.subscribe(()=>this.dispose());this._locationChanges=()=>i.unsubscribe()}else this._config.disposeOnNavigation===`url-change`&&(this._locationChanges=this._location.onUrlChange(()=>this.dispose()));return this._outsideClickDispatcher.add(this),typeof e?.onDestroy==`function`&&e.onDestroy(()=>{this.hasAttached()&&this._ngZone.runOutsideAngular(()=>Promise.resolve().then(()=>this.detach()))}),e}detach(){if(!this.hasAttached())return;this.detachBackdrop(),this._togglePointerEvents(!1),this._positionStrategy&&this._positionStrategy.detach&&this._positionStrategy.detach(),this._scrollStrategy&&this._scrollStrategy.disable();let n=this._portalOutlet.detach();return this._detachments.next(),this._completeDetachContent(),this._keyboardDispatcher.remove(this),this._detachContentWhenEmpty(),this._locationChanges?.(),this._outsideClickDispatcher.remove(this),tt(()=>{lb.update(e=>e.filter(i=>i!==this))}),n}dispose(){if(this._disposed)return;let n=this.hasAttached();this._positionStrategy&&this._positionStrategy.dispose(),this._disposeScrollStrategy(),this._backdropRef?.dispose(),this._locationChanges?.(),this._keyboardDispatcher.remove(this),this._portalOutlet.dispose(),this._attachments.complete(),this._backdropClick.complete(),this._keydownEvents.complete(),this._outsidePointerEvents.complete(),this._outsideClickDispatcher.remove(this),this._host?.remove(),this._afterNextRenderRef?.destroy(),this._previousHostParent=this._pane=this._host=this._backdropRef=null,n&&this._detachments.next(),this._detachments.complete(),this._completeDetachContent(),this._disposed=!0,tt(()=>{lb.update(e=>e.filter(i=>i!==this))})}hasAttached(){return this._portalOutlet.hasAttached()}backdropClick(){return this._backdropClick}attachments(){return this._attachments}detachments(){return this._detachments}keydownEvents(){return this._keydownEvents}outsidePointerEvents(){return this._outsidePointerEvents}getConfig(){return this._config}updatePosition(){this._positionStrategy&&this._positionStrategy.apply()}updatePositionStrategy(n){n!==this._positionStrategy&&(this._positionStrategy&&this._positionStrategy.dispose(),this._positionStrategy=n,this.hasAttached()&&(n.attach(this),this.updatePosition()))}updateSize(n){this._config=r(r({},this._config),n),this._updateElementSize()}setDirection(n){this._config=s(r({},this._config),{direction:n}),this._updateElementDirection()}addPanelClass(n){this._pane&&this._toggleClasses(this._pane,n,!0)}removePanelClass(n){this._pane&&this._toggleClasses(this._pane,n,!1)}getDirection(){let n=this._config.direction;return n?typeof n==`string`?n:n.value:`ltr`}updateScrollStrategy(n){n!==this._scrollStrategy&&(this._disposeScrollStrategy(),this._scrollStrategy=n,this.hasAttached()&&(n.attach(this),n.enable()))}_updateElementDirection(){this._host.setAttribute(`dir`,this.getDirection())}_updateElementSize(){if(!this._pane)return;let n=this._pane.style;n.width=St(this._config.width),n.height=St(this._config.height),n.minWidth=St(this._config.minWidth),n.minHeight=St(this._config.minHeight),n.maxWidth=St(this._config.maxWidth),n.maxHeight=St(this._config.maxHeight)}_togglePointerEvents(n){this._pane.style.pointerEvents=n?``:`none`}_attachHost(){if(!this._host.parentElement){let n=this._config.usePopover?this._positionStrategy?.getPopoverInsertionPoint?.():null;mb(n)?n.after(this._host):n?.type===`parent`?n.element.appendChild(this._host):this._previousHostParent?.appendChild(this._host)}if(this._config.usePopover)try{this._host.showPopover()}catch{}}_attachBackdrop(){let n=`cdk-overlay-backdrop-showing`;this._backdropRef?.dispose(),this._backdropRef=new ub(this._document,this._renderer,this._ngZone,e=>{this._backdropClick.next(e)}),this._animationsDisabled&&this._backdropRef.element.classList.add(`cdk-overlay-backdrop-noop-animation`),this._config.backdropClass&&this._toggleClasses(this._backdropRef.element,this._config.backdropClass,!0),this._config.usePopover?this._host.prepend(this._backdropRef.element):this._host.parentElement.insertBefore(this._backdropRef.element,this._host),!this._animationsDisabled&&typeof requestAnimationFrame<`u`?this._ngZone.runOutsideAngular(()=>{requestAnimationFrame(()=>this._backdropRef?.element.classList.add(n))}):this._backdropRef.element.classList.add(n)}_updateStackingOrder(){!this._config.usePopover&&this._host.nextSibling&&this._host.parentNode.appendChild(this._host)}detachBackdrop(){this._animationsDisabled?(this._backdropRef?.dispose(),this._backdropRef=null):this._backdropRef?.detach()}_toggleClasses(n,e,i){let a=Ar(e||[]).filter(o=>!!o);a.length&&(i?n.classList.add(...a):n.classList.remove(...a))}_detachContentWhenEmpty(){let n=!1;try{this._detachContentAfterRenderRef=Mt(()=>{n=!0,this._detachContent()},{injector:this._injector})}catch(e){if(n)throw e;this._detachContent()}globalThis.MutationObserver&&this._pane&&(this._detachContentMutationObserver||=new globalThis.MutationObserver(()=>{this._detachContent()}),this._detachContentMutationObserver.observe(this._pane,{childList:!0}))}_detachContent(){(!this._pane||!this._host||this._pane.children.length===0)&&(this._pane&&this._config.panelClass&&this._toggleClasses(this._pane,this._config.panelClass,!1),this._host&&this._host.parentElement&&(this._previousHostParent=this._host.parentElement,this._host.remove()),this._completeDetachContent())}_completeDetachContent(){this._detachContentAfterRenderRef?.destroy(),this._detachContentAfterRenderRef=void 0,this._detachContentMutationObserver?.disconnect()}_disposeScrollStrategy(){let n=this._scrollStrategy;n?.disable(),n?.detach?.()}};var aw=`cdk-overlay-connected-position-bounding-box`;var NO=/([A-Za-z%]+)$/;function Fo(t,n){return new nm(n,t.get(ui),t.get(ce),t.get(Te),t.get(am))}var nm=class{_viewportRuler;_document;_platform;_overlayContainer;_overlayRef;_isInitialRender=!1;_lastBoundingBoxSize={width:0,height:0};_isPushed=!1;_canPush=!0;_growAfterOpen=!1;_hasFlexibleDimensions=!0;_positionLocked=!1;_originRect;_overlayRect;_viewportRect;_containerRect;_viewportMargin=0;_scrollables=[];_preferredPositions=[];_origin;_pane;_isDisposed=!1;_boundingBox=null;_lastPosition=null;_lastScrollVisibility=null;_positionChanges=new L;_resizeSubscription=De.EMPTY;_offsetX=0;_offsetY=0;_transformOriginSelector;_appliedPanelClasses=[];_previousPushAmount=null;_popoverLocation=`global`;positionChanges=this._positionChanges;get positions(){return this._preferredPositions}constructor(n,e,i,a,o){this._viewportRuler=e,this._document=i,this._platform=a,this._overlayContainer=o,this.setOrigin(n)}attach(n){this._overlayRef&&this._overlayRef,this._validatePositions(),n.hostElement.classList.add(aw),this._overlayRef=n,this._boundingBox=n.hostElement,this._pane=n.overlayElement,this._isDisposed=!1,this._isInitialRender=!0,this._lastPosition=null,this._resizeSubscription.unsubscribe(),this._resizeSubscription=this._viewportRuler.change().subscribe(()=>{this._isInitialRender=!0,this.apply()})}apply(){if(this._isDisposed||!this._platform.isBrowser)return;if(!this._isInitialRender&&this._positionLocked&&this._lastPosition){this.reapplyLastPosition();return}this._clearPanelClasses(),this._resetOverlayElementStyles(),this._resetBoundingBoxStyles(),this._viewportRect=this._getNarrowedViewportRect(),this._originRect=this._getOriginRect(),this._overlayRect=this._pane.getBoundingClientRect(),this._containerRect=this._getContainerRect();let n=this._originRect,e=this._overlayRect,i=this._viewportRect,a=this._containerRect,o=[],r;for(let s of this._preferredPositions){let c=this._getOriginPoint(n,a,s),m=this._getOverlayPoint(c,e,s),u=this._getOverlayFit(m,e,i,s);if(u.isCompletelyWithinViewport){this._isPushed=!1,this._applyPosition(s,c);return}if(this._canFitWithFlexibleDimensions(u,m,i)){o.push({position:s,origin:c,overlayRect:e,boundingBoxRect:this._calculateBoundingBoxRect(c,s)});continue}(!r||r.overlayFit.visibleArea<u.visibleArea)&&(r={overlayFit:u,overlayPoint:m,originPoint:c,position:s,overlayRect:e})}if(o.length){let s=null,c=-1;for(let m of o){let u=m.boundingBoxRect.width*m.boundingBoxRect.height*(m.position.weight||1);u>c&&(c=u,s=m)}this._isPushed=!1,this._applyPosition(s.position,s.origin);return}if(this._canPush){this._isPushed=!0,this._applyPosition(r.position,r.originPoint);return}this._applyPosition(r.position,r.originPoint)}detach(){this._clearPanelClasses(),this._lastPosition=null,this._previousPushAmount=null,this._resizeSubscription.unsubscribe()}dispose(){this._isDisposed||(this._boundingBox&&Po(this._boundingBox.style,{top:``,left:``,right:``,bottom:``,height:``,width:``,alignItems:``,justifyContent:``}),this._pane&&this._resetOverlayElementStyles(),this._overlayRef&&this._overlayRef.hostElement.classList.remove(aw),this.detach(),this._positionChanges.complete(),this._overlayRef=this._boundingBox=null,this._isDisposed=!0)}reapplyLastPosition(){if(this._isDisposed||!this._platform.isBrowser)return;let n=this._lastPosition;n?(this._originRect=this._getOriginRect(),this._overlayRect=this._pane.getBoundingClientRect(),this._viewportRect=this._getNarrowedViewportRect(),this._containerRect=this._getContainerRect(),this._applyPosition(n,this._getOriginPoint(this._originRect,this._containerRect,n))):this.apply()}withScrollableContainers(n){return this._scrollables=n,this}withPositions(n){return this._preferredPositions=n,n.indexOf(this._lastPosition)===-1&&(this._lastPosition=null),this._validatePositions(),this}withViewportMargin(n){return this._viewportMargin=n,this}withFlexibleDimensions(n=!0){return this._hasFlexibleDimensions=n,this}withGrowAfterOpen(n=!0){return this._growAfterOpen=n,this}withPush(n=!0){return this._canPush=n,this}withLockedPosition(n=!0){return this._positionLocked=n,this}setOrigin(n){return this._origin=n,this}withDefaultOffsetX(n){return this._offsetX=n,this}withDefaultOffsetY(n){return this._offsetY=n,this}withTransformOriginOn(n){return this._transformOriginSelector=n,this}withPopoverLocation(n){return this._popoverLocation=n,this}getPopoverInsertionPoint(){return this._popoverLocation===`global`?null:this._popoverLocation!==`inline`?this._popoverLocation:this._origin instanceof G?this._origin.nativeElement:mb(this._origin)?this._origin:null}_getOriginPoint(n,e,i){let a;if(i.originX==`center`)a=n.left+n.width/2;else{let r=this._isRtl()?n.right:n.left,s=this._isRtl()?n.left:n.right;a=i.originX==`start`?r:s}e.left<0&&(a-=e.left);let o;return i.originY==`center`?o=n.top+n.height/2:o=i.originY==`top`?n.top:n.bottom,e.top<0&&(o-=e.top),{x:a,y:o}}_getOverlayPoint(n,e,i){let a;i.overlayX==`center`?a=-e.width/2:i.overlayX===`start`?a=this._isRtl()?-e.width:0:a=this._isRtl()?0:-e.width;let o;return i.overlayY==`center`?o=-e.height/2:o=i.overlayY==`top`?0:-e.height,{x:n.x+a,y:n.y+o}}_getOverlayFit(n,e,i,a){let o=rw(e),{x:r,y:s}=n,c=this._getOffset(a,`x`),m=this._getOffset(a,`y`);c&&(r+=c),m&&(s+=m);let u=0-r,g=r+o.width-i.width,v=0-s,b=s+o.height-i.height,y=this._subtractOverflows(o.width,u,g),E=this._subtractOverflows(o.height,v,b),R=y*E;return{visibleArea:R,isCompletelyWithinViewport:o.width*o.height===R,fitsInViewportVertically:E===o.height,fitsInViewportHorizontally:y==o.width}}_canFitWithFlexibleDimensions(n,e,i){if(this._hasFlexibleDimensions){let a=i.bottom-e.y,o=i.right-e.x,r=ow(this._overlayRef.getConfig().minHeight),s=ow(this._overlayRef.getConfig().minWidth),c=n.fitsInViewportVertically||r!=null&&r<=a,m=n.fitsInViewportHorizontally||s!=null&&s<=o;return c&&m}return!1}_pushOverlayOnScreen(n,e,i){if(this._previousPushAmount&&this._positionLocked)return{x:n.x+this._previousPushAmount.x,y:n.y+this._previousPushAmount.y};let a=rw(e),o=this._viewportRect,r=Math.max(n.x+a.width-o.width,0),s=Math.max(n.y+a.height-o.height,0),c=Math.max(o.top-i.top-n.y,0),m=Math.max(o.left-i.left-n.x,0),u=0,g=0;return a.width<=o.width?u=m||-r:u=n.x<this._getViewportMarginStart()?o.left-i.left-n.x:0,a.height<=o.height?g=c||-s:g=n.y<this._getViewportMarginTop()?o.top-i.top-n.y:0,this._previousPushAmount={x:u,y:g},{x:n.x+u,y:n.y+g}}_applyPosition(n,e){if(this._setTransformOrigin(n),this._setOverlayElementStyles(e,n),this._setBoundingBoxStyles(e,n),n.panelClass&&this._addPanelClasses(n.panelClass),this._positionChanges.observers.length){let i=this._getScrollVisibility();if(n!==this._lastPosition||!this._lastScrollVisibility||!TO(this._lastScrollVisibility,i)){let a=new tm(n,i);this._positionChanges.next(a)}this._lastScrollVisibility=i}this._lastPosition=n,this._isInitialRender=!1}_setTransformOrigin(n){if(!this._transformOriginSelector)return;let e=this._boundingBox.querySelectorAll(this._transformOriginSelector),i,a=n.overlayY;n.overlayX===`center`?i=`center`:this._isRtl()?i=n.overlayX===`start`?`right`:`left`:i=n.overlayX===`start`?`left`:`right`;for(let o=0;o<e.length;o++)e[o].style.transformOrigin=`${i} ${a}`}_calculateBoundingBoxRect(n,e){let i=this._viewportRect,a=this._isRtl(),o,r,s;if(e.overlayY===`top`)r=n.y,o=i.height-r+this._getViewportMarginBottom();else if(e.overlayY===`bottom`)s=i.height-n.y+this._getViewportMarginTop()+this._getViewportMarginBottom(),o=i.height-s+this._getViewportMarginTop();else{let b=Math.min(i.bottom-n.y+i.top,n.y),y=this._lastBoundingBoxSize.height;o=b*2,r=n.y-b,o>y&&!this._isInitialRender&&!this._growAfterOpen&&(r=n.y-y/2)}let c=e.overlayX===`start`&&!a||e.overlayX===`end`&&a,m=e.overlayX===`end`&&!a||e.overlayX===`start`&&a,u,g,v;if(m)v=i.width-n.x+this._getViewportMarginStart()+this._getViewportMarginEnd(),u=n.x-this._getViewportMarginStart();else if(c)g=n.x,u=i.right-n.x-this._getViewportMarginEnd();else{let b=Math.min(i.right-n.x+i.left,n.x),y=this._lastBoundingBoxSize.width;u=b*2,g=n.x-b,u>y&&!this._isInitialRender&&!this._growAfterOpen&&(g=n.x-y/2)}return{top:r,left:g,bottom:s,right:v,width:u,height:o}}_setBoundingBoxStyles(n,e){let i=this._calculateBoundingBoxRect(n,e);!this._isInitialRender&&!this._growAfterOpen&&(i.height=Math.min(i.height,this._lastBoundingBoxSize.height),i.width=Math.min(i.width,this._lastBoundingBoxSize.width));let a={};if(this._hasExactPosition())a.top=a.left=`0`,a.bottom=a.right=`auto`,a.maxHeight=a.maxWidth=``,a.width=a.height=`100%`;else{let o=this._overlayRef.getConfig().maxHeight,r=this._overlayRef.getConfig().maxWidth;a.width=St(i.width),a.height=St(i.height),a.top=St(i.top)||`auto`,a.bottom=St(i.bottom)||`auto`,a.left=St(i.left)||`auto`,a.right=St(i.right)||`auto`,e.overlayX===`center`?a.alignItems=`center`:a.alignItems=e.overlayX===`end`?`flex-end`:`flex-start`,e.overlayY===`center`?a.justifyContent=`center`:a.justifyContent=e.overlayY===`bottom`?`flex-end`:`flex-start`,o&&(a.maxHeight=St(o)),r&&(a.maxWidth=St(r))}this._lastBoundingBoxSize=i,Po(this._boundingBox.style,a)}_resetBoundingBoxStyles(){Po(this._boundingBox.style,{top:`0`,left:`0`,right:`0`,bottom:`0`,height:``,width:``,alignItems:``,justifyContent:``})}_resetOverlayElementStyles(){Po(this._pane.style,{top:``,left:``,bottom:``,right:``,position:``,transform:``})}_setOverlayElementStyles(n,e){let i={},a=this._hasExactPosition(),o=this._hasFlexibleDimensions,r=this._overlayRef.getConfig();if(a){let u=this._viewportRuler.getViewportScrollPosition();Po(i,this._getExactOverlayY(e,n,u)),Po(i,this._getExactOverlayX(e,n,u))}else i.position=`static`;let s=``,c=this._getOffset(e,`x`),m=this._getOffset(e,`y`);c&&(s+=`translateX(${c}px) `),m&&(s+=`translateY(${m}px)`),i.transform=s.trim(),r.maxHeight&&(a?i.maxHeight=St(r.maxHeight):o&&(i.maxHeight=``)),r.maxWidth&&(a?i.maxWidth=St(r.maxWidth):o&&(i.maxWidth=``)),Po(this._pane.style,i)}_getExactOverlayY(n,e,i){let a={top:``,bottom:``},o=this._getOverlayPoint(e,this._overlayRect,n);if(this._isPushed&&(o=this._pushOverlayOnScreen(o,this._overlayRect,i)),n.overlayY===`bottom`)a.bottom=`${this._document.documentElement.clientHeight-(o.y+this._overlayRect.height)}px`;else a.top=St(o.y);return a}_getExactOverlayX(n,e,i){let a={left:``,right:``},o=this._getOverlayPoint(e,this._overlayRect,n);this._isPushed&&(o=this._pushOverlayOnScreen(o,this._overlayRect,i));let r;if(this._isRtl()?r=n.overlayX===`end`?`left`:`right`:r=n.overlayX===`end`?`right`:`left`,r===`right`)a.right=`${this._document.documentElement.clientWidth-(o.x+this._overlayRect.width)}px`;else a.left=St(o.x);return a}_getScrollVisibility(){let n=this._getOriginRect(),e=this._pane.getBoundingClientRect(),i=this._scrollables.map(a=>a.getElementRef().nativeElement.getBoundingClientRect());return{isOriginClipped:nw(n,i),isOriginOutsideView:db(n,i),isOverlayClipped:nw(e,i),isOverlayOutsideView:db(e,i)}}_subtractOverflows(n,...e){return e.reduce((i,a)=>i-Math.max(a,0),n)}_getNarrowedViewportRect(){let n=this._document.documentElement.clientWidth,e=this._document.documentElement.clientHeight,i=this._viewportRuler.getViewportScrollPosition();return{top:i.top+this._getViewportMarginTop(),left:i.left+this._getViewportMarginStart(),right:i.left+n-this._getViewportMarginEnd(),bottom:i.top+e-this._getViewportMarginBottom(),width:n-this._getViewportMarginStart()-this._getViewportMarginEnd(),height:e-this._getViewportMarginTop()-this._getViewportMarginBottom()}}_isRtl(){return this._overlayRef.getDirection()===`rtl`}_hasExactPosition(){return!this._hasFlexibleDimensions||this._isPushed}_getOffset(n,e){return e===`x`?n.offsetX==null?this._offsetX:n.offsetX:n.offsetY==null?this._offsetY:n.offsetY}_validatePositions(){}_addPanelClasses(n){this._pane&&Ar(n).forEach(e=>{e!==``&&this._appliedPanelClasses.indexOf(e)===-1&&(this._appliedPanelClasses.push(e),this._pane.classList.add(e))})}_clearPanelClasses(){this._pane&&(this._appliedPanelClasses.forEach(n=>{this._pane.classList.remove(n)}),this._appliedPanelClasses=[])}_getViewportMarginStart(){return typeof this._viewportMargin==`number`?this._viewportMargin:this._viewportMargin?.start??0}_getViewportMarginEnd(){return typeof this._viewportMargin==`number`?this._viewportMargin:this._viewportMargin?.end??0}_getViewportMarginTop(){return typeof this._viewportMargin==`number`?this._viewportMargin:this._viewportMargin?.top??0}_getViewportMarginBottom(){return typeof this._viewportMargin==`number`?this._viewportMargin:this._viewportMargin?.bottom??0}_getOriginRect(){let n=this._origin;if(n instanceof G)return n.nativeElement.getBoundingClientRect();if(n instanceof Element)return n.getBoundingClientRect();let e=n.width||0,i=n.height||0;return{top:n.y,bottom:n.y+i,left:n.x,right:n.x+e,height:i,width:e}}_getContainerRect(){let n=this._overlayRef.getConfig().usePopover&&this._popoverLocation!==`global`,e=this._overlayContainer.getContainerElement();n&&(e.style.display=`block`);let i=e.getBoundingClientRect();return n&&(e.style.display=``),i}};function Po(t,n){for(let e in n)Object.hasOwn(n,e)&&(t[e]=n[e]);return t}function ow(t){if(typeof t!=`number`&&t!=null){let[n,e]=t.split(NO);return!e||e===`px`?parseFloat(n):null}return t||null}function rw(t){return{top:Math.floor(t.top),right:Math.floor(t.right),bottom:Math.floor(t.bottom),left:Math.floor(t.left),width:Math.floor(t.width),height:Math.floor(t.height)}}function TO(t,n){return t===n?!0:t.isOriginClipped===n.isOriginClipped&&t.isOriginOutsideView===n.isOriginOutsideView&&t.isOverlayClipped===n.isOverlayClipped&&t.isOverlayOutsideView===n.isOverlayOutsideView}var sw=`cdk-global-overlay-wrapper`;function Jr(t){return new im}var im=class{_overlayRef;_cssPosition=`static`;_topOffset=``;_bottomOffset=``;_alignItems=``;_xPosition=``;_xOffset=``;_width=``;_height=``;_isDisposed=!1;attach(n){let e=n.getConfig();this._overlayRef=n,this._width&&!e.width&&n.updateSize({width:this._width}),this._height&&!e.height&&n.updateSize({height:this._height}),n.hostElement.classList.add(sw),this._isDisposed=!1}top(n=``){return this._bottomOffset=``,this._topOffset=n,this._alignItems=`flex-start`,this}left(n=``){return this._xOffset=n,this._xPosition=`left`,this}bottom(n=``){return this._topOffset=``,this._bottomOffset=n,this._alignItems=`flex-end`,this}right(n=``){return this._xOffset=n,this._xPosition=`right`,this}start(n=``){return this._xOffset=n,this._xPosition=`start`,this}end(n=``){return this._xOffset=n,this._xPosition=`end`,this}width(n=``){return this._overlayRef?this._overlayRef.updateSize({width:n}):this._width=n,this}height(n=``){return this._overlayRef?this._overlayRef.updateSize({height:n}):this._height=n,this}centerHorizontally(n=``){return this.left(n),this._xPosition=`center`,this}centerVertically(n=``){return this.top(n),this._alignItems=`center`,this}apply(){if(!this._overlayRef||!this._overlayRef.hasAttached())return;let n=this._overlayRef.overlayElement.style,e=this._overlayRef.hostElement.style,{width:a,height:o,maxWidth:r,maxHeight:s}=this._overlayRef.getConfig(),c=(a===`100%`||a===`100vw`)&&(!r||r===`100%`||r===`100vw`),m=(o===`100%`||o===`100vh`)&&(!s||s===`100%`||s===`100vh`),u=this._xPosition,g=this._xOffset,v=this._overlayRef.getConfig().direction===`rtl`,b=``,y=``,E=``;c?E=`flex-start`:u===`center`?(E=`center`,v?y=g:b=g):v?u===`left`||u===`end`?(E=`flex-end`,b=g):(u===`right`||u===`start`)&&(E=`flex-start`,y=g):u===`left`||u===`start`?(E=`flex-start`,b=g):(u===`right`||u===`end`)&&(E=`flex-end`,y=g),n.position=this._cssPosition,n.marginLeft=c?`0`:b,n.marginTop=m?`0`:this._topOffset,n.marginBottom=this._bottomOffset,n.marginRight=c?`0`:y,e.justifyContent=E,e.alignItems=m?`flex-start`:this._alignItems}dispose(){if(this._isDisposed||!this._overlayRef)return;let n=this._overlayRef.overlayElement.style,e=this._overlayRef.hostElement,i=e.style;e.classList.remove(sw),i.justifyContent=i.alignItems=n.marginTop=n.marginBottom=n.marginLeft=n.marginRight=n.position=``,this._overlayRef=null,this._isDisposed=!0}};var hw=(()=>{class t{_injector=f(re);global(){return Jr()}flexibleConnectedTo(e){return Fo(this._injector,e)}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var Uc=new D(`OVERLAY_DEFAULT_CONFIG`);function ra(t,n){t.get(Ze).load(pw);let e=t.get(am),i=t.get(ce),a=t.get(Le),o=t.get(On),r=t.get(pt),s=t.get(Ae,null,{optional:!0})||t.get(kt).createRenderer(null,null),c=new ia(n),m=t.get(Uc,null,{optional:!0})?.usePopover??!0;c.direction=c.direction||r.value,!i.body||!(`showPopover`in i.body)?c.usePopover=!1:c.usePopover=n?.usePopover??m;let u=i.createElement(`div`),g=i.createElement(`div`);u.id=a.getId(`cdk-overlay-`),u.classList.add(`cdk-overlay-pane`),g.appendChild(u),c.usePopover&&(g.setAttribute(`popover`,`manual`),g.classList.add(`cdk-overlay-popover`));let v=c.usePopover?c.positionStrategy?.getPopoverInsertionPoint?.():null;return mb(v)?v.after(g):v?.type===`parent`?v.element.appendChild(g):e.getContainerElement().appendChild(g),new Qr(new jc(u,o,t),g,u,c,t.get(K),t.get(uw),i,t.get(Jd),t.get(mw),n?.disableAnimations??t.get(Fs,null,{optional:!0})===`NoopAnimations`,t.get(Et),s)}var fw=(()=>{class t{scrollStrategies=f(lw);_positionBuilder=f(hw);_injector=f(re);create(e){return ra(this._injector,e)}position(){return this._positionBuilder}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var AO=[{originX:`start`,originY:`bottom`,overlayX:`start`,overlayY:`top`},{originX:`start`,originY:`top`,overlayX:`start`,overlayY:`bottom`},{originX:`end`,originY:`top`,overlayX:`end`,overlayY:`bottom`},{originX:`end`,originY:`bottom`,overlayX:`end`,overlayY:`top`}];var OO=new D(`cdk-connected-overlay-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(re);return()=>oa(t)}});var Xr=(()=>{class t{elementRef=f(G);static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,selectors:[[``,`cdk-overlay-origin`,``],[``,`overlay-origin`,``],[``,`cdkOverlayOrigin`,``]],exportAs:[`cdkOverlayOrigin`]})}return t})();var gw=new D(`cdk-connected-overlay-default-config`);var om=(()=>{class t{_dir=f(pt,{optional:!0});_injector=f(re);_overlayRef;_templatePortal;_backdropSubscription=De.EMPTY;_attachSubscription=De.EMPTY;_detachSubscription=De.EMPTY;_positionSubscription=De.EMPTY;_offsetX;_offsetY;_position;_scrollStrategyFactory=f(OO);_ngZone=f(K);origin;positions;positionStrategy;get offsetX(){return this._offsetX}set offsetX(e){this._offsetX=e,this._position&&this._updatePositionStrategy(this._position)}get offsetY(){return this._offsetY}set offsetY(e){this._offsetY=e,this._position&&this._updatePositionStrategy(this._position)}width;height;minWidth;minHeight;backdropClass;panelClass;viewportMargin=0;scrollStrategy;open=!1;disableClose=!1;transformOriginSelector;hasBackdrop=!1;lockPosition=!1;flexibleDimensions=!1;growAfterOpen=!1;push=!1;disposeOnNavigation=!1;usePopover;matchWidth=!1;set _config(e){typeof e!=`string`&&this._assignConfig(e)}backdropClick=new J;positionChange=new J;attach=new J;detach=new J;overlayKeydown=new J;overlayOutsideClick=new J;constructor(){let e=f(Ft),i=f(Lt),a=f(gw,{optional:!0}),o=f(Uc,{optional:!0});this.usePopover=o?.usePopover===!1?null:`global`,this._templatePortal=new zn(e,i),this.scrollStrategy=this._scrollStrategyFactory(),a&&this._assignConfig(a)}get overlayRef(){return this._overlayRef}get dir(){return this._dir?this._dir.value:`ltr`}ngOnDestroy(){this._attachSubscription.unsubscribe(),this._detachSubscription.unsubscribe(),this._backdropSubscription.unsubscribe(),this._positionSubscription.unsubscribe(),this._overlayRef?.dispose()}ngOnChanges(e){this._position&&(this._updatePositionStrategy(this._position),this._overlayRef?.updateSize({width:this._getWidth(),minWidth:this.minWidth,height:this.height,minHeight:this.minHeight}),e.origin&&this.open&&this._position.apply()),e.open&&(this.open?this.attachOverlay():this.detachOverlay())}_createOverlay(){(!this.positions||!this.positions.length)&&(this.positions=AO);let e=this._overlayRef=ra(this._injector,this._buildConfig());this._attachSubscription=e.attachments().subscribe(()=>this.attach.emit()),this._detachSubscription=e.detachments().subscribe(()=>this.detach.emit()),e.keydownEvents().subscribe(i=>{this.overlayKeydown.next(i),i.keyCode===27&&!this.disableClose&&!vt(i)&&(i.preventDefault(),this.detachOverlay())}),this._overlayRef.outsidePointerEvents().subscribe(i=>{let a=this._getOriginElement(),o=qt(i);(!a||a!==o&&!a.contains(o))&&this.overlayOutsideClick.next(i)})}_buildConfig(){let e=this._position=this.positionStrategy||this._createPositionStrategy(),i=new ia({direction:this._dir||`ltr`,positionStrategy:e,scrollStrategy:this.scrollStrategy,hasBackdrop:this.hasBackdrop,disposeOnNavigation:this.disposeOnNavigation,usePopover:!!this.usePopover});return(this.height||this.height===0)&&(i.height=this.height),(this.minWidth||this.minWidth===0)&&(i.minWidth=this.minWidth),(this.minHeight||this.minHeight===0)&&(i.minHeight=this.minHeight),this.backdropClass&&(i.backdropClass=this.backdropClass),this.panelClass&&(i.panelClass=this.panelClass),i}_updatePositionStrategy(e){let i=this.positions.map(a=>({originX:a.originX,originY:a.originY,overlayX:a.overlayX,overlayY:a.overlayY,offsetX:a.offsetX||this.offsetX,offsetY:a.offsetY||this.offsetY,panelClass:a.panelClass||void 0}));return e.setOrigin(this._getOrigin()).withPositions(i).withFlexibleDimensions(this.flexibleDimensions).withPush(this.push).withGrowAfterOpen(this.growAfterOpen).withViewportMargin(this.viewportMargin).withLockedPosition(this.lockPosition).withTransformOriginOn(this.transformOriginSelector).withPopoverLocation(this.usePopover===null?`global`:this.usePopover)}_createPositionStrategy(){let e=Fo(this._injector,this._getOrigin());return this._updatePositionStrategy(e),e}_getOrigin(){return this.origin instanceof Xr?this.origin.elementRef:this.origin}_getOriginElement(){return this.origin instanceof Xr?this.origin.elementRef.nativeElement:this.origin instanceof G?this.origin.nativeElement:typeof Element<`u`&&this.origin instanceof Element?this.origin:null}_getWidth(){return this.width?this.width:this.matchWidth?this._getOriginElement()?.getBoundingClientRect?.().width:void 0}attachOverlay(){this._overlayRef||this._createOverlay();let e=this._overlayRef;e.getConfig().hasBackdrop=this.hasBackdrop,e.updateSize({width:this._getWidth()}),e.hasAttached()||e.attach(this._templatePortal),this.hasBackdrop?this._backdropSubscription=e.backdropClick().subscribe(i=>this.backdropClick.emit(i)):this._backdropSubscription.unsubscribe(),this._positionSubscription.unsubscribe(),this.positionChange.observers.length>0&&(this._positionSubscription=this._position.positionChanges.pipe(op(()=>this.positionChange.observers.length>0)).subscribe(i=>{this._ngZone.run(()=>this.positionChange.emit(i)),this.positionChange.observers.length===0&&this._positionSubscription.unsubscribe()})),this.open=!0}detachOverlay(){this._overlayRef?.detach(),this._backdropSubscription.unsubscribe(),this._positionSubscription.unsubscribe(),this.open=!1}_assignConfig(e){this.origin=e.origin??this.origin,this.positions=e.positions??this.positions,this.positionStrategy=e.positionStrategy??this.positionStrategy,this.offsetX=e.offsetX??this.offsetX,this.offsetY=e.offsetY??this.offsetY,this.width=e.width??this.width,this.height=e.height??this.height,this.minWidth=e.minWidth??this.minWidth,this.minHeight=e.minHeight??this.minHeight,this.backdropClass=e.backdropClass??this.backdropClass,this.panelClass=e.panelClass??this.panelClass,this.viewportMargin=e.viewportMargin??this.viewportMargin,this.scrollStrategy=e.scrollStrategy??this.scrollStrategy,this.disableClose=e.disableClose??this.disableClose,this.transformOriginSelector=e.transformOriginSelector??this.transformOriginSelector,this.hasBackdrop=e.hasBackdrop??this.hasBackdrop,this.lockPosition=e.lockPosition??this.lockPosition,this.flexibleDimensions=e.flexibleDimensions??this.flexibleDimensions,this.growAfterOpen=e.growAfterOpen??this.growAfterOpen,this.push=e.push??this.push,this.disposeOnNavigation=e.disposeOnNavigation??this.disposeOnNavigation,this.usePopover=e.usePopover??this.usePopover,this.matchWidth=e.matchWidth??this.matchWidth}static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,selectors:[[``,`cdk-connected-overlay`,``],[``,`connected-overlay`,``],[``,`cdkConnectedOverlay`,``]],inputs:{origin:[0,`cdkConnectedOverlayOrigin`,`origin`],positions:[0,`cdkConnectedOverlayPositions`,`positions`],positionStrategy:[0,`cdkConnectedOverlayPositionStrategy`,`positionStrategy`],offsetX:[0,`cdkConnectedOverlayOffsetX`,`offsetX`],offsetY:[0,`cdkConnectedOverlayOffsetY`,`offsetY`],width:[0,`cdkConnectedOverlayWidth`,`width`],height:[0,`cdkConnectedOverlayHeight`,`height`],minWidth:[0,`cdkConnectedOverlayMinWidth`,`minWidth`],minHeight:[0,`cdkConnectedOverlayMinHeight`,`minHeight`],backdropClass:[0,`cdkConnectedOverlayBackdropClass`,`backdropClass`],panelClass:[0,`cdkConnectedOverlayPanelClass`,`panelClass`],viewportMargin:[0,`cdkConnectedOverlayViewportMargin`,`viewportMargin`],scrollStrategy:[0,`cdkConnectedOverlayScrollStrategy`,`scrollStrategy`],open:[0,`cdkConnectedOverlayOpen`,`open`],disableClose:[0,`cdkConnectedOverlayDisableClose`,`disableClose`],transformOriginSelector:[0,`cdkConnectedOverlayTransformOriginOn`,`transformOriginSelector`],hasBackdrop:[2,`cdkConnectedOverlayHasBackdrop`,`hasBackdrop`,V],lockPosition:[2,`cdkConnectedOverlayLockPosition`,`lockPosition`,V],flexibleDimensions:[2,`cdkConnectedOverlayFlexibleDimensions`,`flexibleDimensions`,V],growAfterOpen:[2,`cdkConnectedOverlayGrowAfterOpen`,`growAfterOpen`,V],push:[2,`cdkConnectedOverlayPush`,`push`,V],disposeOnNavigation:[2,`cdkConnectedOverlayDisposeOnNavigation`,`disposeOnNavigation`,e=>e===`url-change`||e===`pop-state`?e:V(e)],usePopover:[0,`cdkConnectedOverlayUsePopover`,`usePopover`],matchWidth:[2,`cdkConnectedOverlayMatchWidth`,`matchWidth`,V],_config:[0,`cdkConnectedOverlay`,`_config`]},outputs:{backdropClick:`backdropClick`,positionChange:`positionChange`,attach:`attach`,detach:`detach`,overlayKeydown:`overlayKeydown`,overlayOutsideClick:`overlayOutsideClick`},exportAs:[`cdkConnectedOverlay`],features:[ft]})}return t})();var mi=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({providers:[fw],imports:[we,Ro,cb,cb]})}return t})();var hb=new D(`MAT_MENU_PANEL`);var sa=(()=>{class t{_isAnchor;_elementRef=f(G);_document=f(ce);_focusMonitor=f(Xt);_parentMenu=f(hb,{optional:!0});_changeDetectorRef=f(Re);role=`menuitem`;disabled=!1;disabledInteractive=!1;disableRipple=!1;_hovered=new L;_focused=new L;_highlighted=!1;_triggersSubmenu=!1;constructor(){f(Ze).load(cn),this._parentMenu?.addItem?.(this),this._isAnchor=this._elementRef.nativeElement.tagName===`A`}focus(e,i){this._focusMonitor&&e?this._focusMonitor.focusVia(this._getHostElement(),e,i):this._getHostElement().focus(i),this._focused.next(this)}ngAfterViewInit(){this._focusMonitor&&this._focusMonitor.monitor(this._elementRef,!1)}ngOnDestroy(){this._focusMonitor&&this._focusMonitor.stopMonitoring(this._elementRef),this._parentMenu&&this._parentMenu.removeItem&&this._parentMenu.removeItem(this),this._hovered.complete(),this._focused.complete()}_getTabIndex(){return this.disabled&&!this.disabledInteractive?`-1`:`0`}_getAriaDisabled(){return this._isAnchor?this.disabled||null:this.disabled&&this.disabledInteractive?!0:null}_getDisabled(){return this.disabledInteractive||!this.disabled?null:!0}_getHostElement(){return this._elementRef.nativeElement}_checkDisabled(e){this.disabled&&(e.preventDefault(),e.stopPropagation())}_handleMouseEnter(){this._hovered.next(this)}getLabel(){let e=this._elementRef.nativeElement.cloneNode(!0),i=e.querySelectorAll(`mat-icon, .material-icons, .material-symbols-outlined, .material-symbols-rounded, .material-symbols-sharp`);for(let a=0;a<i.length;a++)i[a].remove();return e.textContent?.trim()||``}_setHighlighted(e){this._highlighted=e,this._changeDetectorRef.markForCheck()}_setTriggersSubmenu(e){this._triggersSubmenu=e,this._changeDetectorRef.markForCheck()}_hasFocus(){return this._document&&this._document.activeElement===this._getHostElement()}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[[[`mat-icon`],[``,`matMenuItemIcon`,``]],`*`],i=[`mat-icon, [matMenuItemIcon]`,`*`];function a(o,r){o&1&&($t(),l(0,`svg`,2),z(1,`polygon`,3),d())}return B({type:t,selectors:[[``,`mat-menu-item`,``]],hostAttrs:[1,`mat-mdc-menu-item`,`mat-focus-indicator`],hostVars:12,hostBindings:function(r,s){r&1&&C(`click`,function(m){return s._checkDisabled(m)})(`mouseenter`,function(){return s._handleMouseEnter()}),r&2&&(ee(`role`,s.role)(`tabindex`,s._getTabIndex())(`aria-disabled`,s._getAriaDisabled())(`disabled`,s._getDisabled()),W(`mat-mdc-menu-item-highlighted`,s._highlighted)(`mat-mdc-menu-item-submenu-trigger`,s._triggersSubmenu)(`mat-mdc-menu-item-disabled`,s.disabled)(`mat-mdc-menu-item-disabled-interactive`,s.disabledInteractive))},inputs:{role:`role`,disabled:[2,`disabled`,`disabled`,V],disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,V],disableRipple:[2,`disableRipple`,`disableRipple`,V]},exportAs:[`matMenuItem`],ngContentSelectors:i,decls:5,vars:3,consts:[[1,`mat-mdc-menu-item-text`],[`matRipple`,``,1,`mat-mdc-menu-ripple`,3,`matRippleDisabled`,`matRippleTrigger`],[`viewBox`,`0 0 5 10`,`focusable`,`false`,`aria-hidden`,`true`,1,`mat-mdc-menu-submenu-icon`],[`points`,`0,0 5,5 0,10`]],template:function(r,s){r&1&&(Oe(e),ue(0),l(1,`span`,0),ue(2,1),d(),z(3,`div`,1),N(4,a,2,0,`:svg:svg`,2)),r&2&&(h(3),S(`matRippleDisabled`,s.disableRipple||s.disabled)(`matRippleTrigger`,s._getHostElement()),h(),T(s._triggersSubmenu?4:-1))},dependencies:[ci],encapsulation:2})})()}return t})();var RO=new D(`MatMenuContent`);var PO=new D(`mat-menu-default-options`,{providedIn:`root`,factory:()=>({overlapTrigger:!1,xPosition:`after`,yPosition:`below`,backdropClass:`cdk-overlay-transparent-backdrop`})});var pb=`_mat-menu-enter`;var rm=`_mat-menu-exit`;var Ni=(()=>{class t{_elementRef=f(G);_changeDetectorRef=f(Re);_injector=f(re);_keyManager;_xPosition;_yPosition;_firstItemFocusRef;_exitFallbackTimeout;_animationsDisabled=ze();_allItems;_directDescendantItems=new _n;_classList={};_panelAnimationState=`void`;_animationDone=new L;_isAnimating=M(!1);parentMenu;direction;overlayPanelClass;backdropClass;ariaLabel;ariaLabelledby;ariaDescribedby;get xPosition(){return this._xPosition}set xPosition(e){this._xPosition=e,this.setPositionClasses()}get yPosition(){return this._yPosition}set yPosition(e){this._yPosition=e,this.setPositionClasses()}templateRef;items;lazyContent;overlapTrigger=!1;hasBackdrop;get panelClass(){return this._previousPanelClass}set panelClass(e){let i=this._previousPanelClass,a=r({},this._classList);i&&i.length&&i.split(` `).forEach(o=>{a[o]=!1}),this._previousPanelClass=e,e&&e.length&&(e.split(` `).forEach(o=>{a[o]=!0}),this._elementRef.nativeElement.className=``),this._classList=a}_previousPanelClass=``;get classList(){return this.panelClass}set classList(e){this.panelClass=e}closed=new J;close=this.closed;panelId=f(Le).getId(`mat-menu-panel-`);constructor(){let e=f(PO);this.overlayPanelClass=e.overlayPanelClass||``,this._xPosition=e.xPosition,this._yPosition=e.yPosition,this.backdropClass=e.backdropClass,this.overlapTrigger=e.overlapTrigger,this.hasBackdrop=e.hasBackdrop}ngOnInit(){this.setPositionClasses()}ngAfterContentInit(){this._updateDirectDescendants(),this._keyManager=new Ji(this._directDescendantItems).withWrap().withTypeAhead().withHomeAndEnd().skipPredicate(e=>e.disabled&&!e.disabledInteractive),this._keyManager.tabOut.subscribe(()=>this.closed.emit(`tab`)),this._directDescendantItems.changes.pipe(dt(this._directDescendantItems),fn(e=>wt(...e.map(i=>i._focused)))).subscribe(e=>this._keyManager.updateActiveItem(e)),this._directDescendantItems.changes.subscribe(e=>{let i=this._keyManager;if(this._panelAnimationState===`enter`&&i.activeItem?._hasFocus()){let a=e.toArray(),o=Math.max(0,Math.min(a.length-1,i.activeItemIndex||0));a[o]&&!a[o].disabled?i.setActiveItem(o):i.setNextItemActive()}})}ngOnDestroy(){this._keyManager?.destroy(),this._directDescendantItems.destroy(),this.closed.complete(),this._firstItemFocusRef?.destroy(),clearTimeout(this._exitFallbackTimeout)}_hovered(){return this._directDescendantItems.changes.pipe(dt(this._directDescendantItems),fn(i=>wt(...i.map(a=>a._hovered))))}addItem(e){}removeItem(e){}_handleKeydown(e){let i=e.keyCode,a=this._keyManager;switch(i){case 27:vt(e)||(e.preventDefault(),this.closed.emit(`keydown`));break;case 37:this.parentMenu&&this.direction===`ltr`&&this.closed.emit(`keydown`);break;case 39:this.parentMenu&&this.direction===`rtl`&&this.closed.emit(`keydown`);break;default:(i===38||i===40)&&a.setFocusOrigin(`keyboard`),a.onKeydown(e);return}}focusFirstItem(e=`program`){this._firstItemFocusRef?.destroy(),this._firstItemFocusRef=Mt(()=>{let i=this._resolvePanel();if(!i||!i.contains(document.activeElement)){let a=this._keyManager;a.setFocusOrigin(e).setFirstItemActive(),!a.activeItem&&i&&i.focus()}},{injector:this._injector})}resetActiveItem(){this._keyManager.setActiveItem(-1)}setElevation(e){}setPositionClasses(e=this.xPosition,i=this.yPosition){this._classList=s(r({},this._classList),{"mat-menu-before":e===`before`,"mat-menu-after":e===`after`,"mat-menu-above":i===`above`,"mat-menu-below":i===`below`}),this._changeDetectorRef.markForCheck()}_onAnimationDone(e){let i=e===rm;(i||e===pb)&&(i&&(clearTimeout(this._exitFallbackTimeout),this._exitFallbackTimeout=void 0),this._animationDone.next(i?`void`:`enter`),this._isAnimating.set(!1))}_onAnimationStart(e){(e===pb||e===rm)&&this._isAnimating.set(!0)}_setIsOpen(e){if(this._panelAnimationState=e?`enter`:`void`,e){if(this._keyManager.activeItemIndex===0){let i=this._resolvePanel();i&&(i.scrollTop=0)}}else this._animationsDisabled||(this._exitFallbackTimeout=setTimeout(()=>this._onAnimationDone(rm),200));this._animationsDisabled&&setTimeout(()=>{this._onAnimationDone(e?pb:rm)}),this._changeDetectorRef.markForCheck()}_updateDirectDescendants(){this._allItems.changes.pipe(dt(this._allItems)).subscribe(e=>{this._directDescendantItems.reset(e.filter(i=>i._parentMenu===this)),this._directDescendantItems.notifyOnChanges()})}_resolvePanel(){let e=null;return this._directDescendantItems.length&&(e=this._directDescendantItems.first._getHostElement().closest(`[role="menu"]`)),e}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`];function i(a,o){if(a&1){let r=ae();rt(0,`div`,0),Dr(`click`,function(){A(r);return O(_().closed.emit(`click`))})(`animationstart`,function(c){A(r);return O(_()._onAnimationStart(c.animationName))})(`animationend`,function(c){A(r);return O(_()._onAnimationDone(c.animationName))})(`animationcancel`,function(c){A(r);return O(_()._onAnimationDone(c.animationName))}),rt(1,`div`,1),ue(2),ut()()}if(a&2){let r=_();et(r._classList),W(`mat-menu-panel-animations-disabled`,r._animationsDisabled)(`mat-menu-panel-exit-animation`,r._panelAnimationState===`void`)(`mat-menu-panel-animating`,r._isAnimating()),Wt(`id`,r.panelId),ee(`aria-label`,r.ariaLabel||null)(`aria-labelledby`,r.ariaLabelledby||null)(`aria-describedby`,r.ariaDescribedby||null)}}return B({type:t,selectors:[[`mat-menu`]],contentQueries:function(o,r,s){if(o&1&&Vt(s,RO,5)(s,sa,5)(s,sa,4),o&2){let c;X(c=Z())&&(r.lazyContent=c.first),X(c=Z())&&(r._allItems=c),X(c=Z())&&(r.items=c)}},viewQuery:function(o,r){if(o&1&&Xe(Ft,5),o&2){let s;X(s=Z())&&(r.templateRef=s.first)}},hostVars:3,hostBindings:function(o,r){o&2&&ee(`aria-label`,null)(`aria-labelledby`,null)(`aria-describedby`,null)},inputs:{backdropClass:`backdropClass`,ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],ariaDescribedby:[0,`aria-describedby`,`ariaDescribedby`],xPosition:`xPosition`,yPosition:`yPosition`,overlapTrigger:[2,`overlapTrigger`,`overlapTrigger`,V],hasBackdrop:[2,`hasBackdrop`,`hasBackdrop`,a=>a==null?null:V(a)],panelClass:[0,`class`,`panelClass`],classList:`classList`},outputs:{closed:`closed`,close:`close`},exportAs:[`matMenu`],features:[$e([{provide:hb,useExisting:t}])],ngContentSelectors:e,decls:1,vars:0,consts:[[`tabindex`,`-1`,`role`,`menu`,1,`mat-mdc-menu-panel`,3,`click`,`animationstart`,`animationend`,`animationcancel`,`id`],[1,`mat-mdc-menu-content`]],template:function(o,r){o&1&&(Oe(),wr(0,i,3,12,`ng-template`))},styles:[`mat-menu {
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
`],encapsulation:2})})()}return t})();var FO=new D(`mat-menu-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(re);return()=>oa(t)}});var es=new WeakMap;var LO=(()=>{class t{_canHaveBackdrop;_element=f(G);_viewContainerRef=f(Lt);_menuItemInstance=f(sa,{optional:!0,self:!0});_dir=f(pt,{optional:!0});_focusMonitor=f(Xt);_ngZone=f(K);_injector=f(re);_scrollStrategy=f(FO);_changeDetectorRef=f(Re);_animationsDisabled=ze();_portal;_overlayRef=null;_menuOpen=!1;_closingActionsSubscription=De.EMPTY;_menuCloseSubscription=De.EMPTY;_pendingRemoval;_parentMaterialMenu;_parentInnerPadding;_openedBy=void 0;get _menu(){return this._menuInternal}set _menu(e){e!==this._menuInternal&&(this._menuInternal=e,this._menuCloseSubscription.unsubscribe(),e?(this._parentMaterialMenu,this._menuCloseSubscription=e.close.subscribe(i=>{this._destroyMenu(i),(i===`click`||i===`tab`)&&this._parentMaterialMenu&&this._parentMaterialMenu.closed.emit(i)})):this._destroyMenu(),this._menuItemInstance?._setTriggersSubmenu(this._triggersSubmenu()))}_menuInternal=null;constructor(e){this._canHaveBackdrop=e;let i=f(hb,{optional:!0});this._parentMaterialMenu=i instanceof Ni?i:void 0}ngOnDestroy(){this._menu&&this._ownsMenu(this._menu)&&es.delete(this._menu),this._pendingRemoval?.unsubscribe(),this._menuCloseSubscription.unsubscribe(),this._closingActionsSubscription.unsubscribe(),this._overlayRef&&(this._overlayRef.dispose(),this._overlayRef=null)}get menuOpen(){return this._menuOpen}get dir(){return this._dir&&this._dir.value===`rtl`?`rtl`:`ltr`}_triggersSubmenu(){return!!(this._menuItemInstance&&this._parentMaterialMenu&&this._menu)}_closeMenu(){this._menu?.close.emit()}_openMenu(e){if(this._triggerIsAriaDisabled())return;let i=this._menu;if(this._menuOpen||!i)return;this._pendingRemoval?.unsubscribe();let a=es.get(i);es.set(i,this),a&&a!==this&&a._closeMenu();let o=this._createOverlay(i),r=o.getConfig(),s=r.positionStrategy;this._setPosition(i,s),this._canHaveBackdrop?r.hasBackdrop=i.hasBackdrop==null?!this._triggersSubmenu():i.hasBackdrop:r.hasBackdrop=i.hasBackdrop??!1,o.hasAttached()||(o.attach(this._getPortal(i)),i.lazyContent?.attach(this.menuData)),this._closingActionsSubscription=this._menuClosingActions().subscribe(()=>this._closeMenu()),i.parentMenu=this._triggersSubmenu()?this._parentMaterialMenu:void 0,i.direction=this.dir,e&&i.focusFirstItem(this._openedBy||`program`),this._setIsMenuOpen(!0),i instanceof Ni&&(i._setIsOpen(!0),i._directDescendantItems.changes.pipe(Me(i.close)).subscribe(()=>{s.withLockedPosition(!1).reapplyLastPosition(),s.withLockedPosition(!0)}))}focus(e,i){this._focusMonitor&&e?this._focusMonitor.focusVia(this._element,e,i):this._element.nativeElement.focus(i)}_destroyMenu(e){let i=this._overlayRef,a=this._menu;!i||!this.menuOpen||(this._closingActionsSubscription.unsubscribe(),this._pendingRemoval?.unsubscribe(),a instanceof Ni&&this._ownsMenu(a)?(this._pendingRemoval=a._animationDone.pipe(hn(1)).subscribe(()=>{i.detach(),es.has(a)||a.lazyContent?.detach()}),a._setIsOpen(!1)):(i.detach(),a?.lazyContent?.detach()),a&&this._ownsMenu(a)&&es.delete(a),this.restoreFocus&&(e===`keydown`||!this._openedBy||!this._triggersSubmenu())&&this.focus(this._openedBy),this._openedBy=void 0,this._setIsMenuOpen(!1))}_setIsMenuOpen(e){e!==this._menuOpen&&(this._menuOpen=e,this._menuOpen?this.menuOpened.emit():this.menuClosed.emit(),this._triggersSubmenu()&&this._menuItemInstance._setHighlighted(e),this._changeDetectorRef.markForCheck())}_createOverlay(e){if(!this._overlayRef){let i=this._getOverlayConfig(e);this._subscribeToPositions(e,i.positionStrategy),this._overlayRef=ra(this._injector,i),this._overlayRef.keydownEvents().subscribe(a=>{this._menu instanceof Ni&&this._menu._handleKeydown(a)})}return this._overlayRef}_getOverlayConfig(e){return new ia({positionStrategy:Fo(this._injector,this._getOverlayOrigin()).withLockedPosition().withGrowAfterOpen().withTransformOriginOn(`.mat-menu-panel, .mat-mdc-menu-panel`),backdropClass:e.backdropClass||`cdk-overlay-transparent-backdrop`,panelClass:e.overlayPanelClass,scrollStrategy:this._scrollStrategy(),direction:this._dir||`ltr`,disableAnimations:this._animationsDisabled})}_subscribeToPositions(e,i){e.setPositionClasses&&i.positionChanges.subscribe(a=>{this._ngZone.run(()=>{let o=a.connectionPair.overlayX===`start`?`after`:`before`,r=a.connectionPair.overlayY===`top`?`below`:`above`;e.setPositionClasses(o,r)})})}_setPosition(e,i){let[a,o]=e.xPosition===`before`?[`end`,`start`]:[`start`,`end`],[r,s]=e.yPosition===`above`?[`bottom`,`top`]:[`top`,`bottom`],[c,m]=[r,s],[u,g]=[a,o],v=0;if(this._triggersSubmenu()){if(g=a=e.xPosition===`before`?`start`:`end`,o=u=a===`end`?`start`:`end`,this._parentMaterialMenu){if(this._parentInnerPadding==null){let b=this._parentMaterialMenu.items.first;this._parentInnerPadding=b?b._getHostElement().offsetTop:0}v=r===`bottom`?this._parentInnerPadding:-this._parentInnerPadding}}else e.overlapTrigger||(c=r===`top`?`bottom`:`top`,m=s===`top`?`bottom`:`top`);i.withPositions([{originX:a,originY:c,overlayX:u,overlayY:r,offsetY:v},{originX:o,originY:c,overlayX:g,overlayY:r,offsetY:v},{originX:a,originY:m,overlayX:u,overlayY:s,offsetY:-v},{originX:o,originY:m,overlayX:g,overlayY:s,offsetY:-v}])}_menuClosingActions(){let e=this._getOutsideClickStream(this._overlayRef),i=this._overlayRef.detachments();return wt(e,this._parentMaterialMenu?this._parentMaterialMenu.closed:In(),this._parentMaterialMenu?this._parentMaterialMenu._hovered().pipe(Ke(r=>this._menuOpen&&r!==this._menuItemInstance)):In(),i)}_getPortal(e){return(!this._portal||this._portal.templateRef!==e.templateRef)&&(this._portal=new zn(e.templateRef,this._viewContainerRef)),this._portal}_ownsMenu(e){return es.get(e)===this}_triggerIsAriaDisabled(){return V(this._element.nativeElement.getAttribute(`aria-disabled`))}static ɵfac=function(i){Nf()};static ɵdir=q({type:t})}return t})();var ts=(()=>{class t extends LO{_cleanupTouchstart;_hoverSubscription=De.EMPTY;get _deprecatedMatMenuTriggerFor(){return this.menu}set _deprecatedMatMenuTriggerFor(e){this.menu=e}get menu(){return this._menu}set menu(e){this._menu=e}menuData;restoreFocus=!0;menuOpened=new J;onMenuOpen=this.menuOpened;menuClosed=new J;onMenuClose=this.menuClosed;constructor(){super(!0);let e=f(Ae);this._cleanupTouchstart=e.listen(this._element.nativeElement,`touchstart`,i=>{wo(i)||(this._openedBy=`touch`)},{passive:!0})}triggersSubmenu(){return super._triggersSubmenu()}toggleMenu(){return this.menuOpen?this.closeMenu():this.openMenu()}openMenu(){this._openMenu(!0)}closeMenu(){this._closeMenu()}updatePosition(){this._overlayRef?.updatePosition()}ngAfterContentInit(){this._handleHover()}ngOnDestroy(){super.ngOnDestroy(),this._cleanupTouchstart(),this._hoverSubscription.unsubscribe()}_getOverlayOrigin(){return this._element}_getOutsideClickStream(e){return e.backdropClick()}_handleMousedown(e){So(e)||(this._openedBy=e.button===0?`mouse`:void 0,this.triggersSubmenu()&&e.preventDefault())}_handleKeydown(e){let i=e.keyCode;(i===13||i===32)&&(this._openedBy=`keyboard`),this.triggersSubmenu()&&(i===39&&this.dir===`ltr`||i===37&&this.dir===`rtl`)&&(this._openedBy=`keyboard`,this.openMenu())}_handleClick(e){this.triggersSubmenu()?(e.stopPropagation(),this.openMenu()):this.toggleMenu()}_handleHover(){this.triggersSubmenu()&&this._parentMaterialMenu&&(this._hoverSubscription=this._parentMaterialMenu._hovered().subscribe(e=>{e===this._menuItemInstance&&!e.disabled&&this._parentMaterialMenu?._panelAnimationState!==`void`&&(this._openedBy=`mouse`,this._openMenu(!1))}))}static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,selectors:[[``,`mat-menu-trigger-for`,``],[``,`matMenuTriggerFor`,``]],hostAttrs:[1,`mat-mdc-menu-trigger`],hostVars:3,hostBindings:function(i,a){i&1&&C(`click`,function(r){return a._handleClick(r)})(`mousedown`,function(r){return a._handleMousedown(r)})(`keydown`,function(r){return a._handleKeydown(r)}),i&2&&ee(`aria-haspopup`,a.menu?`menu`:null)(`aria-expanded`,a.menuOpen)(`aria-controls`,a.menuOpen?a.menu?.panelId:null)},inputs:{_deprecatedMatMenuTriggerFor:[0,`mat-menu-trigger-for`,`_deprecatedMatMenuTriggerFor`],menu:[0,`matMenuTriggerFor`,`menu`],menuData:[0,`matMenuTriggerData`,`menuData`],restoreFocus:[0,`matMenuTriggerRestoreFocus`,`restoreFocus`]},outputs:{menuOpened:`menuOpened`,onMenuOpen:`onMenuOpen`,menuClosed:`menuClosed`,onMenuClose:`onMenuClose`},exportAs:[`matMenuTrigger`],features:[Fe]})}return t})();var ns=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({imports:[ki,mi,we,na]})}return t})();var Lo=class{_multiple;_emitChanges;compareWith;_selection=new Set;_deselectedToEmit=[];_selectedToEmit=[];_selected=null;get selected(){return this._selected||(this._selected=Array.from(this._selection.values())),this._selected}changed=new L;bulk={select:n=>this._select(n),deselect:n=>this._deselect(n),setSelection:n=>this._setSelection(n)};constructor(n=!1,e,i=!0,a){this._multiple=n,this._emitChanges=i,this.compareWith=a,e&&e.length&&(n?e.forEach(o=>this._markSelected(o)):this._markSelected(e[0]),this._selectedToEmit.length=0)}select(...n){return this._select(n)}deselect(...n){return this._deselect(n)}setSelection(...n){return this._setSelection(n)}toggle(n){return this.isSelected(n)?this.deselect(n):this.select(n)}clear(n=!0){this._unmarkAll();let e=this._hasQueuedChanges();return n&&this._emitChangeEvent(),e}isSelected(n){return this._selection.has(this._getConcreteValue(n))}isEmpty(){return this._selection.size===0}hasValue(){return!this.isEmpty()}sort(n){this._multiple&&this.selected&&this._selected.sort(n)}isMultipleSelection(){return this._multiple}_select(n){this._verifyValueAssignment(n),n.forEach(i=>this._markSelected(i));let e=this._hasQueuedChanges();return this._emitChangeEvent(),e}_deselect(n){this._verifyValueAssignment(n),n.forEach(i=>this._unmarkSelected(i));let e=this._hasQueuedChanges();return this._emitChangeEvent(),e}_setSelection(n){this._verifyValueAssignment(n);let e=this.selected,i=new Set(n.map(o=>this._getConcreteValue(o)));n.forEach(o=>this._markSelected(o)),e.filter(o=>!i.has(this._getConcreteValue(o,i))).forEach(o=>this._unmarkSelected(o));let a=this._hasQueuedChanges();return this._emitChangeEvent(),a}_emitChangeEvent(){this._selected=null,(this._selectedToEmit.length||this._deselectedToEmit.length)&&(this.changed.next({source:this,added:this._selectedToEmit,removed:this._deselectedToEmit}),this._deselectedToEmit=[],this._selectedToEmit=[])}_markSelected(n){n=this._getConcreteValue(n),this.isSelected(n)||(this._multiple||this._unmarkAll(),this.isSelected(n)||this._selection.add(n),this._emitChanges&&this._selectedToEmit.push(n))}_unmarkSelected(n){n=this._getConcreteValue(n),this.isSelected(n)&&(this._selection.delete(n),this._emitChanges&&this._deselectedToEmit.push(n))}_unmarkAll(){this.isEmpty()||this._selection.forEach(n=>this._unmarkSelected(n))}_verifyValueAssignment(n){n.length>1&&this._multiple}_hasQueuedChanges(){return!!(this._deselectedToEmit.length||this._selectedToEmit.length)}_getConcreteValue(n,e){if(this.compareWith){e=e??this._selection;for(let i of e)if(this.compareWith(n,i))return i;return n}else return n}};var sm=(()=>{class t{_animationsDisabled=ze();state=`unchecked`;disabled=!1;appearance=`full`;static ɵfac=function(i){return new(i||t)};static ɵcmp=B({type:t,selectors:[[`mat-pseudo-checkbox`]],hostAttrs:[1,`mat-pseudo-checkbox`],hostVars:12,hostBindings:function(i,a){i&2&&W(`mat-pseudo-checkbox-indeterminate`,a.state===`indeterminate`)(`mat-pseudo-checkbox-checked`,a.state===`checked`)(`mat-pseudo-checkbox-disabled`,a.disabled)(`mat-pseudo-checkbox-minimal`,a.appearance===`minimal`)(`mat-pseudo-checkbox-full`,a.appearance===`full`)(`_mat-animation-noopable`,a._animationsDisabled)},inputs:{state:`state`,disabled:`disabled`,appearance:`appearance`},decls:0,vars:0,template:function(i,a){},styles:[`.mat-pseudo-checkbox {
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
`],encapsulation:2})}return t})();var bb=new D(`MAT_OPTION_PARENT_COMPONENT`);var vb=new D(`MatOptgroup`);var gb=class{source;isUserInput;constructor(n,e=!1){this.source=n,this.isUserInput=e}};var Hn=(()=>{class t{_element=f(G);_changeDetectorRef=f(Re);_parent=f(bb,{optional:!0});group=f(vb,{optional:!0});_signalDisableRipple=!1;_selected=!1;_active=!1;_mostRecentViewValue=``;get multiple(){return this._parent&&this._parent.multiple}get selected(){return this._selected}value;id=f(Le).getId(`mat-option-`);get disabled(){return this.group&&this.group.disabled||this._disabled()}set disabled(e){this._disabled.set(e)}_disabled=M(!1);get disableRipple(){return this._signalDisableRipple?this._parent.disableRipple():!!this._parent?.disableRipple}get hideSingleSelectionIndicator(){return!!(this._parent&&this._parent.hideSingleSelectionIndicator)}onSelectionChange=new J;_text;_stateChanges=new L;constructor(){let e=f(Ze);e.load(cn),e.load(Di),this._signalDisableRipple=!!this._parent&&on(this._parent.disableRipple)}get active(){return this._active}get viewValue(){return(this._text?.nativeElement.textContent||``).trim()}select(e=!0){this._selected||(this._selected=!0,this._changeDetectorRef.markForCheck(),e&&this._emitSelectionChangeEvent())}deselect(e=!0){this._selected&&(this._selected=!1,this._changeDetectorRef.markForCheck(),e&&this._emitSelectionChangeEvent())}focus(e,i){let a=this._getHostElement();typeof a.focus==`function`&&a.focus(i)}setActiveStyles(){this._active||(this._active=!0,this._changeDetectorRef.markForCheck())}setInactiveStyles(){this._active&&(this._active=!1,this._changeDetectorRef.markForCheck())}getLabel(){return this.viewValue}_handleKeydown(e){(e.keyCode===13||e.keyCode===32)&&!vt(e)&&(this._selectViaInteraction(),e.preventDefault())}_selectViaInteraction(){this.disabled||(this._selected=this.multiple?!this._selected:!0,this._changeDetectorRef.markForCheck(),this._emitSelectionChangeEvent(!0))}_getTabIndex(){return this.disabled?`-1`:`0`}_getHostElement(){return this._element.nativeElement}ngAfterViewChecked(){if(this._selected){let e=this.viewValue;e!==this._mostRecentViewValue&&(this._mostRecentViewValue&&this._stateChanges.next(),this._mostRecentViewValue=e)}}ngOnDestroy(){this._stateChanges.complete()}_emitSelectionChangeEvent(e=!1){this.onSelectionChange.emit(new gb(this,e))}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`text`],i=[[[`mat-icon`]],`*`],a=[`mat-icon`,`*`];function o(c,m){if(c&1&&z(0,`mat-pseudo-checkbox`,1),c&2){let u=_();S(`disabled`,u.disabled)(`state`,u.selected?`checked`:`unchecked`)}}function r(c,m){if(c&1&&z(0,`mat-pseudo-checkbox`,3),c&2)S(`disabled`,_().disabled)}function s(c,m){if(c&1&&(l(0,`span`,4),p(1),d()),c&2){let u=_();h(),H(`(`,u.group.label,`)`)}}return B({type:t,selectors:[[`mat-option`]],viewQuery:function(m,u){if(m&1&&Xe(e,7),m&2){let g;X(g=Z())&&(u._text=g.first)}},hostAttrs:[`role`,`option`,1,`mat-mdc-option`,`mdc-list-item`],hostVars:11,hostBindings:function(m,u){m&1&&C(`click`,function(){return u._selectViaInteraction()})(`keydown`,function(v){return u._handleKeydown(v)}),m&2&&(Wt(`id`,u.id),ee(`aria-selected`,u.selected)(`aria-disabled`,u.disabled.toString()),W(`mdc-list-item--selected`,u.selected)(`mat-mdc-option-multiple`,u.multiple)(`mat-mdc-option-active`,u.active)(`mdc-list-item--disabled`,u.disabled))},inputs:{value:`value`,id:`id`,disabled:[2,`disabled`,`disabled`,V]},outputs:{onSelectionChange:`onSelectionChange`},exportAs:[`matOption`],ngContentSelectors:a,decls:8,vars:5,consts:[[`text`,``],[`aria-hidden`,`true`,1,`mat-mdc-option-pseudo-checkbox`,3,`disabled`,`state`],[1,`mdc-list-item__primary-text`],[`state`,`checked`,`aria-hidden`,`true`,`appearance`,`minimal`,1,`mat-mdc-option-pseudo-checkbox`,3,`disabled`],[1,`cdk-visually-hidden`],[`aria-hidden`,`true`,`mat-ripple`,``,1,`mat-mdc-option-ripple`,`mat-focus-indicator`,3,`matRippleTrigger`,`matRippleDisabled`]],template:function(m,u){m&1&&(Oe(i),N(0,o,1,2,`mat-pseudo-checkbox`,1),ue(1),l(2,`span`,2,0),ue(4,1),d(),N(5,r,1,1,`mat-pseudo-checkbox`,3),N(6,s,2,1,`span`,4),z(7,`div`,5)),m&2&&(T(u.multiple?0:-1),h(5),T(!u.multiple&&u.selected&&!u.hideSingleSelectionIndicator?5:-1),h(),T(u.group&&u.group._inert?6:-1),h(),S(`matRippleTrigger`,u._getHostElement())(`matRippleDisabled`,u.disabled||u.disableRipple))},dependencies:[sm,ci],styles:[`.mat-mdc-option {
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
`],encapsulation:2})})()}return t})();function bw(t,n,e){if(e.length){let i=n.toArray(),a=e.toArray(),o=0;for(let r=0;r<t+1;r++)i[r].group&&i[r].group===a[o]&&o++;return o}return 0}function vw(t,n,e,i){return t<e?t:t+n>e+i?Math.max(0,t-i+n):e}var yw=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({imports:[we]})}return t})();var yb=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({imports:[ki,yw,Hn,we]})}return t})();var VO=new D(`mat-select-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(re);return()=>oa(t)}});var BO=new D(`MAT_SELECT_CONFIG`);var jO=new D(`MatSelectTrigger`);var _b=class{source;value;constructor(n,e){this.source=n,this.value=e}};var Oa=(()=>{class t{_viewportRuler=f(ui);_changeDetectorRef=f(Re);_elementRef=f(G);_dir=f(pt,{optional:!0});_idGenerator=f(Le);_renderer=f(Ae);_parentFormField=f(Fc,{optional:!0});ngControl=f(Ii,{self:!0,optional:!0});_liveAnnouncer=f(Og);_defaultOptions=f(BO,{optional:!0});_animationsDisabled=ze();_popoverLocation;_initialized=new L;_cleanupDetach;options;optionGroups;customTrigger;_positions=[{originX:`start`,originY:`bottom`,overlayX:`start`,overlayY:`top`},{originX:`end`,originY:`bottom`,overlayX:`end`,overlayY:`top`},{originX:`start`,originY:`top`,overlayX:`start`,overlayY:`bottom`,panelClass:`mat-mdc-select-panel-above`},{originX:`end`,originY:`top`,overlayX:`end`,overlayY:`bottom`,panelClass:`mat-mdc-select-panel-above`}];_scrollOptionIntoView(e){let i=this.options.toArray()[e];if(i){let a=this.panel.nativeElement,o=bw(e,this.options,this.optionGroups),r=i._getHostElement();e===0&&o===1?a.scrollTop=0:a.scrollTop=vw(r.offsetTop,r.offsetHeight,a.scrollTop,a.offsetHeight)}}_positioningSettled(){this._scrollOptionIntoView(this._keyManager.activeItemIndex||0)}_getChangeEvent(e){return new _b(this,e)}_scrollStrategyFactory=f(VO);_panelOpen=!1;_compareWith=(e,i)=>e===i;_uid=this._idGenerator.getId(`mat-select-`);_triggerAriaLabelledBy=null;_previousControl;_destroy=new L;_errorStateTracker;stateChanges=new L;disableAutomaticLabeling=!0;userAriaDescribedBy;_selectionModel;_keyManager;_preferredOverlayOrigin;_overlayWidth;_onChange=()=>{};_onTouched=()=>{};_valueId=this._idGenerator.getId(`mat-select-value-`);_scrollStrategy;_overlayPanelClass=this._defaultOptions?.overlayPanelClass||``;get focused(){return this._focused||this._panelOpen}_focused=!1;controlType=`mat-select`;trigger;panel;_overlayDir;panelClass;disabled=!1;get disableRipple(){return this._disableRipple()}set disableRipple(e){this._disableRipple.set(e)}_disableRipple=M(!1);tabIndex=0;get hideSingleSelectionIndicator(){return this._hideSingleSelectionIndicator}set hideSingleSelectionIndicator(e){this._hideSingleSelectionIndicator=e,this._syncParentProperties()}_hideSingleSelectionIndicator=this._defaultOptions?.hideSingleSelectionIndicator??!1;get placeholder(){return this._placeholder}set placeholder(e){this._placeholder=e,this.stateChanges.next()}_placeholder;get required(){return this._required??this.ngControl?.control?.hasValidator(Ao.required)??!1}set required(e){this._required=e,this.stateChanges.next()}_required;get multiple(){return this._multiple}set multiple(e){this._selectionModel,this._multiple=e}_multiple=!1;disableOptionCentering=this._defaultOptions?.disableOptionCentering??!1;get compareWith(){return this._compareWith}set compareWith(e){this._compareWith=e,this._selectionModel&&this._initializeSelection()}get value(){return this._value}set value(e){this._assignValue(e)&&this._onChange(e)}_value;ariaLabel=``;ariaLabelledby;get errorStateMatcher(){return this._errorStateTracker.matcher}set errorStateMatcher(e){this._errorStateTracker.matcher=e}typeaheadDebounceInterval;sortComparator;get id(){return this._id}set id(e){this._id=e||this._uid,this.stateChanges.next()}_id;get errorState(){return this._errorStateTracker.errorState}set errorState(e){this._errorStateTracker.errorState=e}panelWidth=this._defaultOptions&&typeof this._defaultOptions.panelWidth<`u`?this._defaultOptions.panelWidth:`auto`;canSelectNullableOptions=this._defaultOptions?.canSelectNullableOptions??!1;optionSelectionChanges=Ya(()=>{let e=this.options;return e?e.changes.pipe(dt(e),fn(()=>wt(...e.map(i=>i.onSelectionChange)))):this._initialized.pipe(fn(()=>this.optionSelectionChanges))});openedChange=new J;_openedStream=this.openedChange.pipe(Ke(e=>e),ot(()=>{}));_closedStream=this.openedChange.pipe(Ke(e=>!e),ot(()=>{}));selectionChange=new J;valueChange=new J;constructor(){let e=f(Gr),i=f(Ac,{optional:!0}),a=f(Oc,{optional:!0}),o=f(new Yi(`tabindex`),{optional:!0}),r=f(Uc,{optional:!0}),s=f(Wu,{optional:!0,self:!0});this.ngControl&&(this.ngControl.valueAccessor=this),this._defaultOptions?.typeaheadDebounceInterval!=null&&(this.typeaheadDebounceInterval=this._defaultOptions.typeaheadDebounceInterval),this._errorStateTracker=new Wr(e,s||this.ngControl,a,i,this.stateChanges),this._scrollStrategy=this._scrollStrategyFactory(),this.tabIndex=o==null?0:parseInt(o)||0,this._popoverLocation=r?.usePopover===!1?null:`inline`,this.id=this.id}ngOnInit(){this._selectionModel=new Lo(this.multiple),this.stateChanges.next(),this._viewportRuler.change().pipe(Me(this._destroy)).subscribe(()=>{this.panelOpen&&(this._overlayWidth=this._getOverlayWidth(this._preferredOverlayOrigin),this._changeDetectorRef.detectChanges())})}ngAfterContentInit(){this._initialized.next(),this._initialized.complete(),this._initKeyManager(),this._selectionModel.changed.pipe(Me(this._destroy)).subscribe(e=>{e.added.forEach(i=>i.select()),e.removed.forEach(i=>i.deselect())}),this.options.changes.pipe(dt(null),Me(this._destroy)).subscribe(()=>{this._resetOptions(),this._initializeSelection()})}ngDoCheck(){let e=this._getTriggerAriaLabelledby(),i=this.ngControl;if(e!==this._triggerAriaLabelledBy){let a=this._elementRef.nativeElement;this._triggerAriaLabelledBy=e,e?a.setAttribute(`aria-labelledby`,e):a.removeAttribute(`aria-labelledby`)}i&&(this._previousControl!==i.control&&(this._previousControl!==void 0&&i.disabled!==null&&i.disabled!==this.disabled&&(this.disabled=i.disabled),this._previousControl=i.control),this.updateErrorState())}ngOnChanges(e){(e.disabled||e.userAriaDescribedBy)&&this.stateChanges.next(),e.typeaheadDebounceInterval&&this._keyManager&&this._keyManager.withTypeAhead(this.typeaheadDebounceInterval),e.panelClass&&this.panelClass instanceof Set&&(this.panelClass=Array.from(this.panelClass))}ngOnDestroy(){this._cleanupDetach?.(),this._keyManager?.destroy(),this._destroy.next(),this._destroy.complete(),this.stateChanges.complete()}toggle(){this.panelOpen?this.close():this.open()}open(){this._canOpen()&&(this._parentFormField&&(this._preferredOverlayOrigin=this._parentFormField.getConnectedOverlayOrigin()),this._cleanupDetach?.(),this._overlayWidth=this._getOverlayWidth(this._preferredOverlayOrigin),this._panelOpen=!0,this._overlayDir.positionChange.pipe(hn(1)).subscribe(()=>{this._changeDetectorRef.detectChanges(),this._positioningSettled()}),this._overlayDir.attachOverlay(),this._keyManager.withHorizontalOrientation(null),this._highlightCorrectOption(),this._changeDetectorRef.markForCheck(),this.stateChanges.next(),Promise.resolve().then(()=>this.openedChange.emit(!0)))}close(){this._panelOpen&&(this._panelOpen=!1,this._exitAndDetach(),this._keyManager.withHorizontalOrientation(this._isRtl()?`rtl`:`ltr`),this._changeDetectorRef.markForCheck(),this._onTouched(),this.stateChanges.next(),Promise.resolve().then(()=>this.openedChange.emit(!1)))}_exitAndDetach(){if(this._animationsDisabled||!this.panel){this._detachOverlay();return}this._cleanupDetach?.(),this._cleanupDetach=()=>{i(),clearTimeout(a),this._cleanupDetach=void 0};let e=this.panel.nativeElement,i=this._renderer.listen(e,`animationend`,o=>{o.animationName===`_mat-select-exit`&&(this._cleanupDetach?.(),this._detachOverlay())}),a=setTimeout(()=>{this._cleanupDetach?.(),this._detachOverlay()},200);e.classList.add(`mat-select-panel-exit`)}_detachOverlay(){this._overlayDir.detachOverlay(),this._changeDetectorRef.markForCheck()}writeValue(e){this._assignValue(e)}registerOnChange(e){this._onChange=e}registerOnTouched(e){this._onTouched=e}setDisabledState(e){this.disabled=e,this._changeDetectorRef.markForCheck(),this.stateChanges.next()}get panelOpen(){return this._panelOpen}get selected(){return this.multiple?this._selectionModel?.selected||[]:this._selectionModel?.selected[0]}get triggerValue(){if(this.empty)return``;if(this._multiple){let e=this._selectionModel.selected.map(i=>i.viewValue);return this._isRtl()&&e.reverse(),e.join(`, `)}return this._selectionModel.selected[0].viewValue}updateErrorState(){this._errorStateTracker.updateErrorState()}_isRtl(){return this._dir?this._dir.value===`rtl`:!1}_handleKeydown(e){this.disabled||(this.panelOpen?this._handleOpenKeydown(e):this._handleClosedKeydown(e))}_handleClosedKeydown(e){let i=e.keyCode,a=i===40||i===38||i===37||i===39,o=i===13||i===32,r=this._keyManager;if(!r.isTyping()&&o&&!vt(e)||(this.multiple||e.altKey)&&a)e.preventDefault(),this.open();else if(!this.multiple){let s=this.selected;r.onKeydown(e);let c=this.selected;c&&s!==c&&this._liveAnnouncer.announce(c.viewValue,1e4)}}_handleOpenKeydown(e){let i=this._keyManager,a=e.keyCode,o=a===40||a===38,r=i.isTyping();if(o&&e.altKey)e.preventDefault(),this.close();else if(!r&&(a===13||a===32)&&i.activeItem&&!vt(e))e.preventDefault(),i.activeItem._selectViaInteraction();else if(!r&&this._multiple&&a===65&&e.ctrlKey){e.preventDefault();let s=this.options.some(c=>!c.disabled&&!c.selected);this.options.forEach(c=>{c.disabled||(s?c.select():c.deselect())})}else{let s=i.activeItemIndex;i.onKeydown(e),this._multiple&&o&&e.shiftKey&&i.activeItem&&i.activeItemIndex!==s&&i.activeItem._selectViaInteraction()}}_handleOverlayKeydown(e){e.keyCode===27&&!vt(e)&&(e.preventDefault(),this.close())}_onFocus(){this.disabled||(this._focused=!0,this.stateChanges.next())}_onBlur(){this._focused=!1,this._keyManager?.cancelTypeahead(),!this.disabled&&!this.panelOpen&&(this._onTouched(),this._changeDetectorRef.markForCheck(),this.stateChanges.next())}get empty(){return!this._selectionModel||this._selectionModel.isEmpty()}_initializeSelection(){Promise.resolve().then(()=>{this.ngControl&&(this._value=this.ngControl.value),this._setSelectionByValue(this._value),this.stateChanges.next()})}_setSelectionByValue(e){if(this.options.forEach(i=>i.setInactiveStyles()),this._selectionModel.clear(),this.multiple&&e)e.forEach(i=>this._selectOptionByValue(i)),this._sortValues();else{let i=this._selectOptionByValue(e);i?this._keyManager.updateActiveItem(i):this.panelOpen||this._keyManager.updateActiveItem(-1)}this._changeDetectorRef.markForCheck()}_selectOptionByValue(e){let i=this.options.find(a=>{if(this._selectionModel.isSelected(a))return!1;try{return(a.value!=null||this.canSelectNullableOptions)&&this._compareWith(a.value,e)}catch{return!1}});return i&&this._selectionModel.select(i),i}_assignValue(e){return e!==this._value||this._multiple&&Array.isArray(e)?(this.options&&this._setSelectionByValue(e),this._value=e,!0):!1}_skipPredicate=e=>this.panelOpen?!1:e.disabled;_getOverlayWidth(e){return this.panelWidth===`auto`?(e instanceof Xr?e.elementRef:e||this._elementRef).nativeElement.getBoundingClientRect().width:this.panelWidth===null?``:this.panelWidth}_syncParentProperties(){if(this.options)for(let e of this.options)e._changeDetectorRef.markForCheck()}_initKeyManager(){this._keyManager=new Sc(this.options).withTypeAhead(this.typeaheadDebounceInterval).withVerticalOrientation().withHorizontalOrientation(this._isRtl()?`rtl`:`ltr`).withHomeAndEnd().withPageUpDown().withAllowedModifierKeys([`shiftKey`]).skipPredicate(this._skipPredicate),this._keyManager.tabOut.subscribe(()=>{this.panelOpen&&(!this.multiple&&this._keyManager.activeItem&&this._keyManager.activeItem._selectViaInteraction(),this.focus(),this.close())}),this._keyManager.change.subscribe(()=>{this._panelOpen&&this.panel?this._scrollOptionIntoView(this._keyManager.activeItemIndex||0):!this._panelOpen&&!this.multiple&&this._keyManager.activeItem&&this._keyManager.activeItem._selectViaInteraction()})}_resetOptions(){let e=wt(this.options.changes,this._destroy);this.optionSelectionChanges.pipe(Me(e)).subscribe(i=>{this._onSelect(i.source,i.isUserInput),i.isUserInput&&!this.multiple&&this._panelOpen&&(this.close(),this.focus())}),wt(...this.options.map(i=>i._stateChanges)).pipe(Me(e)).subscribe(()=>{this._changeDetectorRef.detectChanges(),this.stateChanges.next()})}_onSelect(e,i){let a=this._selectionModel.isSelected(e);!this.canSelectNullableOptions&&e.value==null&&!this._multiple?(e.deselect(),this._selectionModel.clear(),this.value!=null&&this._propagateChanges(e.value)):(a!==e.selected&&(e.selected?this._selectionModel.select(e):this._selectionModel.deselect(e)),i&&this._keyManager.setActiveItem(e),this.multiple&&(this._sortValues(),i&&this.focus())),a!==this._selectionModel.isSelected(e)&&this._propagateChanges(),this.stateChanges.next()}_sortValues(){if(this.multiple){let e=this.options.toArray();this._selectionModel.sort((i,a)=>this.sortComparator?this.sortComparator(i,a,e):e.indexOf(i)-e.indexOf(a)),this.stateChanges.next()}}_propagateChanges(e){let i;this.multiple?i=this.selected.map(a=>a.value):i=this.selected?this.selected.value:e,this._value=i,this.valueChange.emit(i),this._onChange(i),this.selectionChange.emit(this._getChangeEvent(i)),this._changeDetectorRef.markForCheck()}_highlightCorrectOption(){if(this._keyManager)if(this.empty){let e=-1;for(let i=0;i<this.options.length;i++)if(!this.options.get(i).disabled){e=i;break}this._keyManager.setActiveItem(e)}else this._keyManager.setActiveItem(this._selectionModel.selected[0])}_canOpen(){return!this._panelOpen&&!this.disabled&&this.options?.length>0&&!!this._overlayDir}focus(e){this._elementRef.nativeElement.focus(e)}_getPanelAriaLabelledby(){if(this.ariaLabel)return null;let e=this._parentFormField?.getLabelId()||null,i=e?e+` `:``;return this.ariaLabelledby?i+this.ariaLabelledby:e}_getAriaActiveDescendant(){return this.panelOpen&&this._keyManager&&this._keyManager.activeItem?this._keyManager.activeItem.id:null}_getTriggerAriaLabelledby(){if(this.ariaLabel)return null;let e=this._parentFormField?.getLabelId()||``;return this.ariaLabelledby&&(e+=` `+this.ariaLabelledby),e||(e=this._valueId),e}get describedByIds(){return this._elementRef.nativeElement.getAttribute(`aria-describedby`)?.split(` `)||[]}setDescribedByIds(e){let i=this._elementRef.nativeElement;e.length?i.setAttribute(`aria-describedby`,e.join(` `)):i.removeAttribute(`aria-describedby`)}onContainerClick(e){let i=qt(e);i&&(i.tagName===`MAT-OPTION`||i.classList.contains(`cdk-overlay-backdrop`)||i.closest(`.mat-mdc-select-panel`))||(this.focus(),this.open())}get shouldLabelFloat(){return this.panelOpen||!this.empty||this.focused&&!!this.placeholder}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`trigger`],i=[`panel`],a=[[[`mat-select-trigger`]],`*`],o=[`mat-select-trigger`,`*`];function r(g,v){if(g&1&&(l(0,`span`,4),p(1),d()),g&2){let b=_();h(),w(b.placeholder)}}function s(g,v){g&1&&ue(0)}function c(g,v){if(g&1&&(l(0,`span`,11),p(1),d()),g&2){let b=_(2);h(),w(b.triggerValue)}}function m(g,v){if(g&1&&(l(0,`span`,5),N(1,s,1,0)(2,c,2,1,`span`,11),d()),g&2){let b=_();h(),T(b.customTrigger?1:2)}}function u(g,v){if(g&1){let b=ae();l(0,`div`,12,1),C(`keydown`,function(E){A(b);return O(_()._handleKeydown(E))}),ue(2,1),d()}if(g&2){let b=_();et(b.panelClass),W(`mat-select-panel-animations-enabled`,!b._animationsDisabled)(`mat-primary`,b._parentFormField?.color===`primary`)(`mat-accent`,b._parentFormField?.color===`accent`)(`mat-warn`,b._parentFormField?.color===`warn`)(`mat-undefined`,!b._parentFormField?.color),ee(`id`,b.id+`-panel`)(`aria-multiselectable`,b.multiple)(`aria-label`,b.ariaLabel||null)(`aria-labelledby`,b._getPanelAriaLabelledby())}}return B({type:t,selectors:[[`mat-select`]],contentQueries:function(v,b,y){if(v&1&&Vt(y,jO,5)(y,Hn,5)(y,vb,5),v&2){let E;X(E=Z())&&(b.customTrigger=E.first),X(E=Z())&&(b.options=E),X(E=Z())&&(b.optionGroups=E)}},viewQuery:function(v,b){if(v&1&&Xe(e,5)(i,5)(om,5),v&2){let y;X(y=Z())&&(b.trigger=y.first),X(y=Z())&&(b.panel=y.first),X(y=Z())&&(b._overlayDir=y.first)}},hostAttrs:[`role`,`combobox`,`aria-haspopup`,`listbox`,1,`mat-mdc-select`],hostVars:21,hostBindings:function(v,b){v&1&&C(`keydown`,function(E){return b._handleKeydown(E)})(`focus`,function(){return b._onFocus()})(`blur`,function(){return b._onBlur()}),v&2&&(ee(`id`,b.id)(`tabindex`,b.disabled?-1:b.tabIndex)(`aria-controls`,b.panelOpen?b.id+`-panel`:null)(`aria-expanded`,b.panelOpen)(`aria-label`,b.ariaLabel||null)(`aria-required`,b.required.toString())(`aria-disabled`,b.disabled.toString())(`aria-invalid`,b.errorState)(`aria-activedescendant`,b._getAriaActiveDescendant()),W(`mat-mdc-select-disabled`,b.disabled)(`mat-mdc-select-invalid`,b.errorState)(`mat-mdc-select-required`,b.required)(`mat-mdc-select-empty`,b.empty)(`mat-mdc-select-multiple`,b.multiple)(`mat-select-open`,b.panelOpen))},inputs:{userAriaDescribedBy:[0,`aria-describedby`,`userAriaDescribedBy`],panelClass:`panelClass`,disabled:[2,`disabled`,`disabled`,V],disableRipple:[2,`disableRipple`,`disableRipple`,V],tabIndex:[2,`tabIndex`,`tabIndex`,g=>g==null?0:st(g)],hideSingleSelectionIndicator:[2,`hideSingleSelectionIndicator`,`hideSingleSelectionIndicator`,V],placeholder:`placeholder`,required:[2,`required`,`required`,V],multiple:[2,`multiple`,`multiple`,V],disableOptionCentering:[2,`disableOptionCentering`,`disableOptionCentering`,V],compareWith:`compareWith`,value:`value`,ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],errorStateMatcher:`errorStateMatcher`,typeaheadDebounceInterval:[2,`typeaheadDebounceInterval`,`typeaheadDebounceInterval`,st],sortComparator:`sortComparator`,id:`id`,panelWidth:`panelWidth`,canSelectNullableOptions:[2,`canSelectNullableOptions`,`canSelectNullableOptions`,V]},outputs:{openedChange:`openedChange`,_openedStream:`opened`,_closedStream:`closed`,selectionChange:`selectionChange`,valueChange:`valueChange`},exportAs:[`matSelect`],features:[$e([{provide:Pc,useExisting:t},{provide:bb,useExisting:t}]),ft],ngContentSelectors:o,decls:11,vars:10,consts:[[`fallbackOverlayOrigin`,`cdkOverlayOrigin`,`trigger`,``],[`panel`,``],[`cdk-overlay-origin`,``,1,`mat-mdc-select-trigger`,3,`click`],[1,`mat-mdc-select-value`],[1,`mat-mdc-select-placeholder`,`mat-mdc-select-min-line`],[1,`mat-mdc-select-value-text`],[1,`mat-mdc-select-arrow-wrapper`],[1,`mat-mdc-select-arrow`],[`viewBox`,`0 0 24 24`,`width`,`24px`,`height`,`24px`,`focusable`,`false`,`aria-hidden`,`true`],[`d`,`M7 10l5 5 5-5z`],[`cdk-connected-overlay`,``,`cdkConnectedOverlayHasBackdrop`,``,`cdkConnectedOverlayBackdropClass`,`cdk-overlay-transparent-backdrop`,3,`detach`,`backdropClick`,`overlayKeydown`,`cdkConnectedOverlayDisableClose`,`cdkConnectedOverlayPanelClass`,`cdkConnectedOverlayScrollStrategy`,`cdkConnectedOverlayOrigin`,`cdkConnectedOverlayPositions`,`cdkConnectedOverlayWidth`,`cdkConnectedOverlayFlexibleDimensions`,`cdkConnectedOverlayUsePopover`],[1,`mat-mdc-select-min-line`],[`role`,`listbox`,`tabindex`,`-1`,1,`mat-mdc-select-panel`,`mdc-menu-surface`,`mdc-menu-surface--open`,3,`keydown`]],template:function(v,b){if(v&1&&(Oe(a),l(0,`div`,2,0),C(`click`,function(){return b.open()}),l(3,`div`,3),N(4,r,2,1,`span`,4)(5,m,3,1,`span`,5),d(),l(6,`div`,6)(7,`div`,7),$t(),l(8,`svg`,8),z(9,`path`,9),d()()()(),at(10,u,3,16,`ng-template`,10),C(`detach`,function(){return b.close()})(`backdropClick`,function(){return b.close()})(`overlayKeydown`,function(E){return b._handleOverlayKeydown(E)})),v&2){let y=mt(1);h(3),ee(`id`,b._valueId),h(),T(b.empty?4:5),h(6),S(`cdkConnectedOverlayDisableClose`,!0)(`cdkConnectedOverlayPanelClass`,b._overlayPanelClass)(`cdkConnectedOverlayScrollStrategy`,b._scrollStrategy)(`cdkConnectedOverlayOrigin`,b._preferredOverlayOrigin||y)(`cdkConnectedOverlayPositions`,b._positions)(`cdkConnectedOverlayWidth`,b._overlayWidth)(`cdkConnectedOverlayFlexibleDimensions`,!0)(`cdkConnectedOverlayUsePopover`,b._popoverLocation)}},dependencies:[Xr,om],styles:[`@keyframes _mat-select-enter {
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
`],encapsulation:2})})()}return t})();var Ra=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({imports:[mi,yb,we,na,di,yb]})}return t})();var zO=20;var HO=new D(`mat-tooltip-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(re);return()=>oa(t,{scrollThrottle:zO})}});var UO=new D(`mat-tooltip-default-options`,{providedIn:`root`,factory:()=>({showDelay:0,hideDelay:0,touchendHideDelay:1500})});var _w=`tooltip-panel`;var $O={passive:!0};var GO=8;var WO=8;var qO=24;var YO=200;var xw=(()=>{class t{_elementRef=f(G);_ngZone=f(K);_platform=f(Te);_ariaDescriber=f(ku);_focusMonitor=f(Xt);_dir=f(pt);_injector=f(re);_viewContainerRef=f(Lt);_mediaMatcher=f(Or);_document=f(ce);_renderer=f(Ae);_animationsDisabled=ze();_defaultOptions=f(UO,{optional:!0});_overlayRef=null;_tooltipInstance=null;_overlayPanelClass;_portal;_position=`below`;_positionAtOrigin=!1;_disabled=!1;_tooltipClass;_viewInitialized=!1;_pointerExitEventsInitialized=!1;_tooltipComponent=KO;_viewportMargin=8;_currentPosition;_cssClassPrefix=`mat-mdc`;_ariaDescriptionPending=!1;_dirSubscribed=!1;get position(){return this._position}set position(e){e!==this._position&&(this._position=e,this._overlayRef&&(this._updatePosition(this._overlayRef),this._tooltipInstance?.show(0),this._overlayRef.updatePosition()))}get positionAtOrigin(){return this._positionAtOrigin}set positionAtOrigin(e){this._positionAtOrigin=ea(e),this._detach(),this._overlayRef=null}get disabled(){return this._disabled}set disabled(e){let i=ea(e);this._disabled!==i&&(this._disabled=i,i?this.hide(0):this._setupPointerEnterEventsIfNeeded(),this._syncAriaDescription(this.message))}get showDelay(){return this._showDelay}set showDelay(e){this._showDelay=ri(e)}_showDelay;get hideDelay(){return this._hideDelay}set hideDelay(e){this._hideDelay=ri(e),this._tooltipInstance&&(this._tooltipInstance._mouseLeaveHideDelay=this._hideDelay)}_hideDelay;touchGestures=`auto`;get message(){return this._message}set message(e){let i=this._message;this._message=e!=null?String(e).trim():``,!this._message&&this._isTooltipVisible()?this.hide(0):(this._setupPointerEnterEventsIfNeeded(),this._updateTooltipMessage()),this._syncAriaDescription(i)}_message=``;get tooltipClass(){return this._tooltipClass}set tooltipClass(e){this._tooltipClass=e,this._tooltipInstance&&this._setTooltipClass(this._tooltipClass)}_eventCleanups=[];_touchstartTimeout=null;_destroyed=new L;_isDestroyed=!1;constructor(){let e=this._defaultOptions;e&&(this._showDelay=e.showDelay,this._hideDelay=e.hideDelay,e.position&&(this.position=e.position),e.positionAtOrigin&&(this.positionAtOrigin=e.positionAtOrigin),e.touchGestures&&(this.touchGestures=e.touchGestures),e.tooltipClass&&(this.tooltipClass=e.tooltipClass)),this._viewportMargin=GO}ngAfterViewInit(){this._viewInitialized=!0,this._setupPointerEnterEventsIfNeeded(),this._focusMonitor.monitor(this._elementRef).pipe(Me(this._destroyed)).subscribe(e=>{e?e===`keyboard`&&this._ngZone.run(()=>this.show()):this._ngZone.run(()=>this.hide(0))})}ngOnDestroy(){let e=this._elementRef.nativeElement;this._touchstartTimeout&&clearTimeout(this._touchstartTimeout),this._overlayRef&&(this._overlayRef.dispose(),this._tooltipInstance=null),this._eventCleanups.forEach(i=>i()),this._eventCleanups.length=0,this._destroyed.next(),this._destroyed.complete(),this._isDestroyed=!0,this._ariaDescriber.removeDescription(e,this.message,`tooltip`),this._focusMonitor.stopMonitoring(e)}show(e=this.showDelay,i){if(this.disabled||!this.message||this._isTooltipVisible()){this._tooltipInstance?._cancelPendingAnimations();return}let a=this._createOverlay(i);this._detach(),this._portal=this._portal||new Na(this._tooltipComponent,this._viewContainerRef);let o=this._tooltipInstance=a.attach(this._portal).instance;o._triggerElement=this._elementRef.nativeElement,o._mouseLeaveHideDelay=this._hideDelay,o.afterHidden().pipe(Me(this._destroyed)).subscribe(()=>this._detach()),this._setTooltipClass(this._tooltipClass),this._updateTooltipMessage(),o.show(e)}hide(e=this.hideDelay){let i=this._tooltipInstance;i&&(i.isVisible()?i.hide(e):(i._cancelPendingAnimations(),this._detach()))}toggle(e){this._isTooltipVisible()?this.hide():this.show(void 0,e)}_isTooltipVisible(){return!!this._tooltipInstance&&this._tooltipInstance.isVisible()}_createOverlay(e){if(this._overlayRef){let r=this._overlayRef.getConfig().positionStrategy;if((!this.positionAtOrigin||!e)&&r._origin instanceof G)return this._overlayRef;this._detach()}let i=this._injector.get(Aa).getAncestorScrollContainers(this._elementRef),a=`${this._cssClassPrefix}-${_w}`,o=Fo(this._injector,this.positionAtOrigin?e||this._elementRef:this._elementRef).withTransformOriginOn(`.${this._cssClassPrefix}-tooltip`).withFlexibleDimensions(!1).withViewportMargin(this._viewportMargin).withScrollableContainers(i).withPopoverLocation(`global`);return o.positionChanges.pipe(Me(this._destroyed)).subscribe(r=>{this._updateCurrentPositionClass(r.connectionPair),this._tooltipInstance&&r.scrollableViewProperties.isOverlayClipped&&this._tooltipInstance.isVisible()&&this._ngZone.run(()=>this.hide(0))}),this._overlayRef=ra(this._injector,{direction:this._dir,positionStrategy:o,panelClass:this._overlayPanelClass?[...this._overlayPanelClass,a]:a,scrollStrategy:this._injector.get(HO)(),disableAnimations:this._animationsDisabled,eventPredicate:this._overlayEventPredicate}),this._updatePosition(this._overlayRef),this._overlayRef.detachments().pipe(Me(this._destroyed)).subscribe(()=>this._detach()),this._overlayRef.outsidePointerEvents().pipe(Me(this._destroyed)).subscribe(()=>this._tooltipInstance?._handleBodyInteraction()),this._overlayRef.keydownEvents().pipe(Me(this._destroyed)).subscribe(r=>{r.preventDefault(),r.stopPropagation(),this._ngZone.run(()=>this.hide(0))}),this._defaultOptions?.disableTooltipInteractivity&&this._overlayRef.addPanelClass(`${this._cssClassPrefix}-tooltip-panel-non-interactive`),this._dirSubscribed||(this._dirSubscribed=!0,this._dir.change.pipe(Me(this._destroyed)).subscribe(()=>{this._overlayRef&&this._updatePosition(this._overlayRef)})),this._overlayRef}_detach(){this._overlayRef&&this._overlayRef.hasAttached()&&this._overlayRef.detach(),this._tooltipInstance=null}_updatePosition(e){let i=e.getConfig().positionStrategy,a=this._getOrigin(),o=this._getOverlayPosition();i.withPositions([this._addOffset(r(r({},a.main),o.main)),this._addOffset(r(r({},a.fallback),o.fallback))])}_addOffset(e){let i=WO,a=!this._dir||this._dir.value==`ltr`;return e.originY===`top`?e.offsetY=-i:e.originY===`bottom`?e.offsetY=i:e.originX===`start`?e.offsetX=a?-i:i:e.originX===`end`&&(e.offsetX=a?i:-i),e}_getOrigin(){let e=!this._dir||this._dir.value==`ltr`,i=this.position,a;i==`above`||i==`below`?a={originX:`center`,originY:i==`above`?`top`:`bottom`}:i==`before`||i==`left`&&e||i==`right`&&!e?a={originX:`start`,originY:`center`}:(i==`after`||i==`right`&&e||i==`left`&&!e)&&(a={originX:`end`,originY:`center`});let{x:o,y:r}=this._invertPosition(a.originX,a.originY);return{main:a,fallback:{originX:o,originY:r}}}_getOverlayPosition(){let e=!this._dir||this._dir.value==`ltr`,i=this.position,a;i==`above`?a={overlayX:`center`,overlayY:`bottom`}:i==`below`?a={overlayX:`center`,overlayY:`top`}:i==`before`||i==`left`&&e||i==`right`&&!e?a={overlayX:`end`,overlayY:`center`}:(i==`after`||i==`right`&&e||i==`left`&&!e)&&(a={overlayX:`start`,overlayY:`center`});let{x:o,y:r}=this._invertPosition(a.overlayX,a.overlayY);return{main:a,fallback:{overlayX:o,overlayY:r}}}_updateTooltipMessage(){this._tooltipInstance&&(this._tooltipInstance.message=this.message,this._tooltipInstance._markForCheck(),Mt(()=>{this._tooltipInstance&&this._overlayRef.updatePosition()},{injector:this._injector}))}_setTooltipClass(e){this._tooltipInstance&&(this._tooltipInstance.tooltipClass=e instanceof Set?Array.from(e):e,this._tooltipInstance._markForCheck())}_invertPosition(e,i){return this.position===`above`||this.position===`below`?i===`top`?i=`bottom`:i===`bottom`&&(i=`top`):e===`end`?e=`start`:e===`start`&&(e=`end`),{x:e,y:i}}_updateCurrentPositionClass(e){let{overlayY:i,originX:a,originY:o}=e,r;if(i===`center`?this._dir&&this._dir.value===`rtl`?r=a===`end`?`left`:`right`:r=a===`start`?`left`:`right`:r=i===`bottom`&&o===`top`?`above`:`below`,r!==this._currentPosition){let s=this._overlayRef;if(s){let c=`${this._cssClassPrefix}-${_w}-`;s.removePanelClass(c+this._currentPosition),s.addPanelClass(c+r)}this._currentPosition=r}}_setupPointerEnterEventsIfNeeded(){this._disabled||!this.message||!this._viewInitialized||this._eventCleanups.length||(this._isTouchPlatform()?this.touchGestures!==`off`&&(this._disableNativeGesturesIfNecessary(),this._addListener(`touchstart`,e=>{let i=e.targetTouches?.[0],a=i?{x:i.clientX,y:i.clientY}:void 0;this._setupPointerExitEventsIfNeeded(),this._touchstartTimeout&&clearTimeout(this._touchstartTimeout);let o=500;this._touchstartTimeout=setTimeout(()=>{this._touchstartTimeout=null,this.show(void 0,a)},this._defaultOptions?.touchLongPressShowDelay??o)})):this._addListener(`mouseenter`,e=>{this._setupPointerExitEventsIfNeeded();let i;e.x!==void 0&&e.y!==void 0&&(i=e),this.show(void 0,i)}))}_setupPointerExitEventsIfNeeded(){if(!this._pointerExitEventsInitialized){if(this._pointerExitEventsInitialized=!0,!this._isTouchPlatform())this._addListener(`mouseleave`,e=>{let i=e.relatedTarget;(!i||!this._overlayRef?.overlayElement.contains(i))&&this.hide()}),this._addListener(`wheel`,e=>{if(this._isTooltipVisible()){let i=this._document.elementFromPoint(e.clientX,e.clientY),a=this._elementRef.nativeElement;i!==a&&!a.contains(i)&&this.hide()}});else if(this.touchGestures!==`off`){this._disableNativeGesturesIfNecessary();let e=()=>{this._touchstartTimeout&&clearTimeout(this._touchstartTimeout),this.hide(this._defaultOptions?.touchendHideDelay)};this._addListener(`touchend`,e),this._addListener(`touchcancel`,e)}}}_addListener(e,i){this._eventCleanups.push(this._renderer.listen(this._elementRef.nativeElement,e,i,$O))}_isTouchPlatform(){let e=this._defaultOptions?.detectHoverCapability;return typeof e==`function`?!e():this._platform.IOS||this._platform.ANDROID?!0:this._platform.isBrowser?!!e&&this._mediaMatcher.matchMedia(`(any-hover: none)`).matches:!1}_disableNativeGesturesIfNecessary(){let e=this.touchGestures;if(e!==`off`){let i=this._elementRef.nativeElement,a=i.style;(e===`on`||i.nodeName!==`INPUT`&&i.nodeName!==`TEXTAREA`)&&(a.userSelect=a.msUserSelect=a.webkitUserSelect=a.MozUserSelect=`none`),(e===`on`||!i.draggable)&&(a.webkitUserDrag=`none`),a.touchAction=`none`,a.webkitTapHighlightColor=`transparent`}}_syncAriaDescription(e){this._ariaDescriptionPending||(this._ariaDescriptionPending=!0,this._ariaDescriber.removeDescription(this._elementRef.nativeElement,e,`tooltip`),this._isDestroyed||Mt({write:()=>{this._ariaDescriptionPending=!1,this.message&&!this.disabled&&this._ariaDescriber.describe(this._elementRef.nativeElement,this.message,`tooltip`)}},{injector:this._injector}))}_overlayEventPredicate=e=>e.type===`keydown`?this._isTooltipVisible()&&e.keyCode===27&&!vt(e):!0;static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,selectors:[[``,`matTooltip`,``]],hostAttrs:[1,`mat-mdc-tooltip-trigger`],hostVars:2,hostBindings:function(i,a){i&2&&W(`mat-mdc-tooltip-disabled`,a.disabled)},inputs:{position:[0,`matTooltipPosition`,`position`],positionAtOrigin:[0,`matTooltipPositionAtOrigin`,`positionAtOrigin`],disabled:[0,`matTooltipDisabled`,`disabled`],showDelay:[0,`matTooltipShowDelay`,`showDelay`],hideDelay:[0,`matTooltipHideDelay`,`hideDelay`],touchGestures:[0,`matTooltipTouchGestures`,`touchGestures`],message:[0,`matTooltip`,`message`],tooltipClass:[0,`matTooltipClass`,`tooltipClass`]},exportAs:[`matTooltip`]})}return t})();var KO=(()=>{class t{_changeDetectorRef=f(Re);_elementRef=f(G);_isMultiline=!1;message;tooltipClass;_showTimeoutId;_hideTimeoutId;_triggerElement;_mouseLeaveHideDelay;_animationsDisabled=ze();_tooltip;_closeOnInteraction=!1;_isVisible=!1;_onHide=new L;_showAnimation=`mat-mdc-tooltip-show`;_hideAnimation=`mat-mdc-tooltip-hide`;show(e){this._hideTimeoutId!=null&&clearTimeout(this._hideTimeoutId),this._showTimeoutId=setTimeout(()=>{this._toggleVisibility(!0),this._showTimeoutId=void 0},e)}hide(e){this._showTimeoutId!=null&&clearTimeout(this._showTimeoutId),this._hideTimeoutId=setTimeout(()=>{this._toggleVisibility(!1),this._hideTimeoutId=void 0},e)}afterHidden(){return this._onHide}isVisible(){return this._isVisible}ngOnDestroy(){this._cancelPendingAnimations(),this._onHide.complete(),this._triggerElement=null}_handleBodyInteraction(){this._closeOnInteraction&&this.hide(0)}_markForCheck(){this._changeDetectorRef.markForCheck()}_handleMouseLeave({relatedTarget:e}){(!e||!this._triggerElement.contains(e))&&(this.isVisible()?this.hide(this._mouseLeaveHideDelay):this._finalizeAnimation(!1))}_onShow(){this._isMultiline=this._isTooltipMultiline(),this._markForCheck()}_isTooltipMultiline(){let e=this._elementRef.nativeElement.getBoundingClientRect();return e.height>qO&&e.width>=YO}_handleAnimationEnd({animationName:e}){(e===this._showAnimation||e===this._hideAnimation)&&this._finalizeAnimation(e===this._showAnimation)}_cancelPendingAnimations(){this._showTimeoutId!=null&&clearTimeout(this._showTimeoutId),this._hideTimeoutId!=null&&clearTimeout(this._hideTimeoutId),this._showTimeoutId=this._hideTimeoutId=void 0}_finalizeAnimation(e){e?this._closeOnInteraction=!0:this.isVisible()||this._onHide.next()}_toggleVisibility(e){let i=this._tooltip.nativeElement,a=this._showAnimation,o=this._hideAnimation;if(i.classList.remove(e?o:a),i.classList.add(e?a:o),this._isVisible!==e&&(this._isVisible=e,this._changeDetectorRef.markForCheck()),e&&!this._animationsDisabled&&typeof getComputedStyle==`function`){let r=getComputedStyle(i);(r.getPropertyValue(`animation-duration`)===`0s`||r.getPropertyValue(`animation-name`)===`none`)&&(this._animationsDisabled=!0)}e&&this._onShow(),this._animationsDisabled&&(i.classList.add(`_mat-animation-noopable`),this._finalizeAnimation(e))}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`tooltip`];return B({type:t,selectors:[[`mat-tooltip-component`]],viewQuery:function(a,o){if(a&1&&Xe(e,7),a&2){let r;X(r=Z())&&(o._tooltip=r.first)}},hostAttrs:[`aria-hidden`,`true`],hostBindings:function(a,o){a&1&&C(`mouseleave`,function(s){return o._handleMouseLeave(s)})},decls:4,vars:5,consts:[[`tooltip`,``],[1,`mdc-tooltip`,`mat-mdc-tooltip`,3,`animationend`],[1,`mat-mdc-tooltip-surface`,`mdc-tooltip__surface`]],template:function(a,o){a&1&&(rt(0,`div`,1,0),Dr(`animationend`,function(s){return o._handleAnimationEnd(s)}),rt(2,`div`,2),p(3),ut()()),a&2&&(et(o.tooltipClass),W(`mdc-tooltip--multiline`,o._isMultiline),h(3),w(o.message))},styles:[`.mat-mdc-tooltip {
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
`],encapsulation:2})})()}return t})();var Cw=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({imports:[Do,mi,we,na]})}return t})();var QO=(()=>{class t{changes=new L;itemsPerPageLabel=`Items per page:`;nextPageLabel=`Next page`;previousPageLabel=`Previous page`;firstPageLabel=`First page`;lastPageLabel=`Last page`;getRangeLabel=(e,i,a)=>{if(a==0||i==0)return`0 of ${a}`;a=Math.max(a,0);let o=e*i,r=o<a?Math.min(o+i,a):o+i;return`${o+1} \u2013 ${r} of ${a}`};static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var XO=50;var ZO=new D(`MAT_PAGINATOR_DEFAULT_OPTIONS`);var $c=(()=>{class t{_intl=f(QO);_changeDetectorRef=f(Re);_formFieldAppearance;_pageSizeLabelId=f(Le).getId(`mat-paginator-page-size-label-`);_intlChanges;_isInitialized=!1;_initializedStream=new qn(1);color;get pageIndex(){return this._pageIndex}set pageIndex(e){this._pageIndex=Math.max(e||0,0),this._changeDetectorRef.markForCheck()}_pageIndex=0;get length(){return this._length}set length(e){this._length=e||0,this._changeDetectorRef.markForCheck()}_length=0;get pageSize(){return this._pageSize}set pageSize(e){this._pageSize=Math.max(e||0,0),this._updateDisplayedPageSizeOptions()}_pageSize;get pageSizeOptions(){return this._pageSizeOptions}set pageSizeOptions(e){this._pageSizeOptions=(e||[]).map(i=>st(i,0)),this._updateDisplayedPageSizeOptions()}_pageSizeOptions=[];hidePageSize=!1;showFirstLastButtons=!1;selectConfig={};disabled=!1;page=new J;_displayedPageSizeOptions;initialized=this._initializedStream;constructor(){let e=this._intl,i=f(ZO,{optional:!0});if(this._intlChanges=e.changes.subscribe(()=>this._changeDetectorRef.markForCheck()),i){let{pageSize:a,pageSizeOptions:o,hidePageSize:r,showFirstLastButtons:s}=i;a!=null&&(this._pageSize=a),o!=null&&(this._pageSizeOptions=o),r!=null&&(this.hidePageSize=r),s!=null&&(this.showFirstLastButtons=s)}this._formFieldAppearance=i?.formFieldAppearance||`outline`}ngOnInit(){this._isInitialized=!0,this._updateDisplayedPageSizeOptions(),this._initializedStream.next()}ngOnDestroy(){this._initializedStream.complete(),this._intlChanges.unsubscribe()}nextPage(){this.hasNextPage()&&this._navigate(this.pageIndex+1)}previousPage(){this.hasPreviousPage()&&this._navigate(this.pageIndex-1)}firstPage(){this.hasPreviousPage()&&this._navigate(0)}lastPage(){this.hasNextPage()&&this._navigate(this.getNumberOfPages()-1)}hasPreviousPage(){return this.pageIndex>=1&&this.pageSize!=0}hasNextPage(){let e=this.getNumberOfPages()-1;return this.pageIndex<e&&this.pageSize!=0}getNumberOfPages(){return this.pageSize?Math.ceil(this.length/this.pageSize):0}_changePageSize(e){let i=this.pageIndex*this.pageSize,a=this.pageIndex;this.pageIndex=Math.floor(i/e)||0,this.pageSize=e,this._emitPageEvent(a)}_nextButtonsDisabled(){return this.disabled||!this.hasNextPage()}_previousButtonsDisabled(){return this.disabled||!this.hasPreviousPage()}_updateDisplayedPageSizeOptions(){this._isInitialized&&(this.pageSize||(this._pageSize=this.pageSizeOptions.length!=0?this.pageSizeOptions[0]:XO),this._displayedPageSizeOptions=this.pageSizeOptions.slice(),this._displayedPageSizeOptions.indexOf(this.pageSize)===-1&&this._displayedPageSizeOptions.push(this.pageSize),this._displayedPageSizeOptions.sort((e,i)=>e-i),this._changeDetectorRef.markForCheck())}_emitPageEvent(e){this.page.emit({previousPageIndex:e,pageIndex:this.pageIndex,pageSize:this.pageSize,length:this.length})}_navigate(e){let i=this.pageIndex;e!==i&&(this.pageIndex=e,this._emitPageEvent(i))}_buttonClicked(e,i){i||this._navigate(e)}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){function e(c,m){if(c&1&&(l(0,`mat-option`,17),p(1),d()),c&2){let u=m.$implicit;S(`value`,u),h(),H(` `,u,` `)}}function i(c,m){if(c&1){let u=ae();l(0,`mat-form-field`,14)(1,`mat-select`,16,0),C(`selectionChange`,function(v){A(u);return O(_(2)._changePageSize(v.value))}),ve(3,e,2,2,`mat-option`,17,ii),d(),l(5,`div`,18),C(`click`,function(){A(u);return O(mt(2).open())}),d()()}if(c&2){let u=_(2);S(`appearance`,u._formFieldAppearance)(`color`,u.color),h(),S(`value`,u.pageSize)(`disabled`,u.disabled),Er(`aria-labelledby`,u._pageSizeLabelId),S(`panelClass`,u.selectConfig.panelClass||``)(`disableOptionCentering`,u.selectConfig.disableOptionCentering),h(2),ye(u._displayedPageSizeOptions)}}function a(c,m){if(c&1&&(l(0,`div`,15),p(1),d()),c&2){let u=_(2);h(),w(u.pageSize)}}function o(c,m){if(c&1&&(l(0,`div`,3)(1,`div`,13),p(2),d(),N(3,i,6,7,`mat-form-field`,14),N(4,a,2,1,`div`,15),d()),c&2){let u=_();h(),ee(`id`,u._pageSizeLabelId),h(),H(` `,u._intl.itemsPerPageLabel,` `),h(),T(u._displayedPageSizeOptions.length>1?3:-1),h(),T(u._displayedPageSizeOptions.length<=1?4:-1)}}function r(c,m){if(c&1){let u=ae();l(0,`button`,19),C(`click`,function(){A(u);let v=_();return O(v._buttonClicked(0,v._previousButtonsDisabled()))}),$t(),l(1,`svg`,8),z(2,`path`,20),d()()}if(c&2){let u=_();S(`matTooltip`,u._intl.firstPageLabel)(`matTooltipDisabled`,u._previousButtonsDisabled())(`disabled`,u._previousButtonsDisabled())(`tabindex`,u._previousButtonsDisabled()?-1:null),ee(`aria-label`,u._intl.firstPageLabel)}}function s(c,m){if(c&1){let u=ae();l(0,`button`,21),C(`click`,function(){A(u);let v=_();return O(v._buttonClicked(v.getNumberOfPages()-1,v._nextButtonsDisabled()))}),$t(),l(1,`svg`,8),z(2,`path`,22),d()()}if(c&2){let u=_();S(`matTooltip`,u._intl.lastPageLabel)(`matTooltipDisabled`,u._nextButtonsDisabled())(`disabled`,u._nextButtonsDisabled())(`tabindex`,u._nextButtonsDisabled()?-1:null),ee(`aria-label`,u._intl.lastPageLabel)}}return B({type:t,selectors:[[`mat-paginator`]],hostAttrs:[`role`,`group`,1,`mat-mdc-paginator`],inputs:{color:`color`,pageIndex:[2,`pageIndex`,`pageIndex`,st],length:[2,`length`,`length`,st],pageSize:[2,`pageSize`,`pageSize`,st],pageSizeOptions:`pageSizeOptions`,hidePageSize:[2,`hidePageSize`,`hidePageSize`,V],showFirstLastButtons:[2,`showFirstLastButtons`,`showFirstLastButtons`,V],selectConfig:`selectConfig`,disabled:[2,`disabled`,`disabled`,V]},outputs:{page:`page`},exportAs:[`matPaginator`],decls:14,vars:14,consts:[[`selectRef`,``],[1,`mat-mdc-paginator-outer-container`],[1,`mat-mdc-paginator-container`],[1,`mat-mdc-paginator-page-size`],[1,`mat-mdc-paginator-range-actions`],[`aria-atomic`,`true`,`aria-live`,`polite`,`role`,`status`,1,`mat-mdc-paginator-range-label`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-first`,3,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-previous`,3,`click`,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`viewBox`,`0 0 24 24`,`focusable`,`false`,`aria-hidden`,`true`,1,`mat-mdc-paginator-icon`],[`d`,`M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-next`,3,`click`,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`d`,`M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-last`,3,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`aria-hidden`,`true`,1,`mat-mdc-paginator-page-size-label`],[1,`mat-mdc-paginator-page-size-select`,3,`appearance`,`color`],[1,`mat-mdc-paginator-page-size-value`],[`hideSingleSelectionIndicator`,``,3,`selectionChange`,`value`,`disabled`,`aria-labelledby`,`panelClass`,`disableOptionCentering`],[3,`value`],[1,`mat-mdc-paginator-touch-target`,3,`click`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-first`,3,`click`,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`d`,`M18.41 16.59L13.82 12l4.59-4.59L17 6l-6 6 6 6zM6 6h2v12H6z`],[`matIconButton`,``,`type`,`button`,`matTooltipPosition`,`above`,`disabledInteractive`,``,1,`mat-mdc-paginator-navigation-last`,3,`click`,`matTooltip`,`matTooltipDisabled`,`disabled`,`tabindex`],[`d`,`M5.59 7.41L10.18 12l-4.59 4.59L7 18l6-6-6-6zM16 6h2v12h-2z`]],template:function(m,u){m&1&&(l(0,`div`,1)(1,`div`,2),N(2,o,5,4,`div`,3),l(3,`div`,4)(4,`div`,5),p(5),d(),N(6,r,3,5,`button`,6),l(7,`button`,7),C(`click`,function(){return u._buttonClicked(u.pageIndex-1,u._previousButtonsDisabled())}),$t(),l(8,`svg`,8),z(9,`path`,9),d()(),va(),l(10,`button`,10),C(`click`,function(){return u._buttonClicked(u.pageIndex+1,u._nextButtonsDisabled())}),$t(),l(11,`svg`,8),z(12,`path`,11),d()(),N(13,s,3,5,`button`,12),d()()()),m&2&&(h(2),T(u.hidePageSize?-1:2),h(3),H(` `,u._intl.getRangeLabel(u.pageIndex,u.pageSize,u.length),` `),h(),T(u.showFirstLastButtons?6:-1),h(),S(`matTooltip`,u._intl.previousPageLabel)(`matTooltipDisabled`,u._previousButtonsDisabled())(`disabled`,u._previousButtonsDisabled())(`tabindex`,u._previousButtonsDisabled()?-1:null),ee(`aria-label`,u._intl.previousPageLabel),h(3),S(`matTooltip`,u._intl.nextPageLabel)(`matTooltipDisabled`,u._nextButtonsDisabled())(`disabled`,u._nextButtonsDisabled())(`tabindex`,u._nextButtonsDisabled()?-1:null),ee(`aria-label`,u._intl.nextPageLabel),h(3),T(u.showFirstLastButtons?13:-1))},dependencies:[Mi,Oa,Hn,Ug,xw],styles:[`.mat-mdc-paginator {
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
`],encapsulation:2})})()}return t})();var cm=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({imports:[Tt,Ra,Cw,$c]})}return t})();var JO=new D(`MAT_PROGRESS_BAR_DEFAULT_OPTIONS`);var is=(()=>{class t{_elementRef=f(G);_ngZone=f(K);_changeDetectorRef=f(Re);_renderer=f(Ae);_cleanupTransitionEnd;constructor(){let e=wc(),i=f(JO,{optional:!0});this._isNoopAnimation=e===`di-disabled`,e===`reduced-motion`&&this._elementRef.nativeElement.classList.add(`mat-progress-bar-reduced-motion`),i&&(i.color&&(this.color=this._defaultColor=i.color),this.mode=i.mode||this.mode)}_isNoopAnimation;get color(){return this._color||this._defaultColor}set color(e){this._color=e}_color;_defaultColor=`primary`;get value(){return this._value}set value(e){this._value=ww(e||0),this._changeDetectorRef.markForCheck()}_value=0;get bufferValue(){return this._bufferValue||0}set bufferValue(e){this._bufferValue=ww(e||0),this._changeDetectorRef.markForCheck()}_bufferValue=0;animationEnd=new J;get mode(){return this._mode}set mode(e){this._mode=e,this._changeDetectorRef.markForCheck()}_mode=`determinate`;ngAfterViewInit(){this._ngZone.runOutsideAngular(()=>{this._cleanupTransitionEnd=this._renderer.listen(this._elementRef.nativeElement,`transitionend`,this._transitionendHandler)})}ngOnDestroy(){this._cleanupTransitionEnd?.()}_getPrimaryBarTransform(){return`scaleX(${this._isIndeterminate()?1:this.value/100})`}_getBufferBarFlexBasis(){return`${this.mode===`buffer`?this.bufferValue:100}%`}_isIndeterminate(){return this.mode===`indeterminate`||this.mode===`query`}_transitionendHandler=e=>{this.animationEnd.observers.length===0||!e.target||!e.target.classList.contains(`mdc-linear-progress__primary-bar`)||(this.mode===`determinate`||this.mode===`buffer`)&&this._ngZone.run(()=>this.animationEnd.next({value:this.value}))};static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){function e(i,a){i&1&&Gt(0,`div`,2)}return B({type:t,selectors:[[`mat-progress-bar`]],hostAttrs:[`role`,`progressbar`,`aria-valuemin`,`0`,`aria-valuemax`,`100`,`tabindex`,`-1`,1,`mat-mdc-progress-bar`,`mdc-linear-progress`],hostVars:10,hostBindings:function(a,o){a&2&&(ee(`aria-valuenow`,o._isIndeterminate()?null:o.value)(`mode`,o.mode),et(`mat-`+o.color),W(`_mat-animation-noopable`,o._isNoopAnimation)(`mdc-linear-progress--animation-ready`,!o._isNoopAnimation)(`mdc-linear-progress--indeterminate`,o._isIndeterminate()))},inputs:{color:`color`,value:[2,`value`,`value`,st],bufferValue:[2,`bufferValue`,`bufferValue`,st],mode:`mode`},outputs:{animationEnd:`animationEnd`},exportAs:[`matProgressBar`],decls:7,vars:5,consts:[[`aria-hidden`,`true`,1,`mdc-linear-progress__buffer`],[1,`mdc-linear-progress__buffer-bar`],[1,`mdc-linear-progress__buffer-dots`],[`aria-hidden`,`true`,1,`mdc-linear-progress__bar`,`mdc-linear-progress__primary-bar`],[1,`mdc-linear-progress__bar-inner`],[`aria-hidden`,`true`,1,`mdc-linear-progress__bar`,`mdc-linear-progress__secondary-bar`]],template:function(a,o){a&1&&(rt(0,`div`,0),Gt(1,`div`,1),N(2,e,1,0,`div`,2),ut(),rt(3,`div`,3),Gt(4,`span`,4),ut(),rt(5,`div`,5),Gt(6,`span`,4),ut()),a&2&&(h(),mn(`flex-basis`,o._getBufferBarFlexBasis()),h(),T(o.mode===`buffer`?2:-1),h(),mn(`transform`,o._getPrimaryBarTransform()))},styles:[`.mat-mdc-progress-bar {
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
`],encapsulation:2})})()}return t})();function ww(t,n=0,e=100){return Math.max(n,Math.min(e,t))}var as=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({imports:[we]})}return t})();var eR=new D(`MatTabContent`);var Gc=(()=>{class t{template=f(Ft);static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,selectors:[[``,`matTabContent`,``]],features:[$e([{provide:eR,useExisting:t}])]})}return t})();var tR=new D(`MatTabLabel`);var Iw=new D(`MAT_TAB`);var nR=(()=>{class t extends ew{_closestTab=f(Iw,{optional:!0});static ɵfac=(()=>{let e;return function(a){return(e||(e=It(t)))(a||t)}})();static ɵdir=q({type:t,selectors:[[``,`mat-tab-label`,``],[``,`matTabLabel`,``]],features:[$e([{provide:tR,useExisting:t}]),Fe]})}return t})();var Mw=new D(`MAT_TAB_GROUP`);var Wc=(()=>{class t{_viewContainerRef=f(Lt);_closestTabGroup=f(Mw,{optional:!0});disabled=!1;get templateLabel(){return this._templateLabel}set templateLabel(e){this._setTemplateLabelInput(e)}_templateLabel;_explicitContent=void 0;_implicitContent;textLabel=``;ariaLabel;ariaLabelledby;labelClass;bodyClass;id=null;_contentPortal=null;get content(){return this._contentPortal}_stateChanges=new L;position=null;origin=null;isActive=!1;constructor(){f(Ze).load(cn)}ngOnChanges(e){(Object.hasOwn(e,`textLabel`)||Object.hasOwn(e,`disabled`))&&this._stateChanges.next()}ngOnDestroy(){this._stateChanges.complete()}ngOnInit(){this._contentPortal=new zn(this._explicitContent||this._implicitContent,this._viewContainerRef)}_setTemplateLabelInput(e){e&&e._closestTab===this&&(this._templateLabel=e)}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`];function i(a,o){a&1&&ue(0)}return B({type:t,selectors:[[`mat-tab`]],contentQueries:function(o,r,s){if(o&1&&Vt(s,nR,5)(s,Gc,7,Ft),o&2){let c;X(c=Z())&&(r.templateLabel=c.first),X(c=Z())&&(r._explicitContent=c.first)}},viewQuery:function(o,r){if(o&1&&Xe(Ft,7),o&2){let s;X(s=Z())&&(r._implicitContent=s.first)}},hostAttrs:[`hidden`,``],hostVars:1,hostBindings:function(o,r){o&2&&ee(`id`,null)},inputs:{disabled:[2,`disabled`,`disabled`,V],textLabel:[0,`label`,`textLabel`],ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],labelClass:`labelClass`,bodyClass:`bodyClass`,id:`id`},exportAs:[`matTab`],features:[$e([{provide:Iw,useExisting:t}]),ft],ngContentSelectors:e,decls:1,vars:0,template:function(o,r){o&1&&(Oe(),wr(0,i,1,0,`ng-template`))},encapsulation:2,changeDetection:1})})()}return t})();var Sb=`mdc-tab-indicator--active`;var Ew=`mdc-tab-indicator--no-transition`;var Eb=class{_items;_currentItem;constructor(n){this._items=n}hide(){this._items.forEach(n=>n.deactivateInkBar()),this._currentItem=void 0}alignToElement(n){let e=this._items.find(a=>a.elementRef.nativeElement===n),i=this._currentItem;if(e!==i&&(i?.deactivateInkBar(),e)){let a=i?.elementRef.nativeElement.getBoundingClientRect?.();e.activateInkBar(a),this._currentItem=e}}};var iR=(()=>{class t{_elementRef=f(G);_inkBarElement=null;_inkBarContentElement=null;_fitToContent=!1;get fitInkBarToContent(){return this._fitToContent}set fitInkBarToContent(e){this._fitToContent!==e&&(this._fitToContent=e,this._inkBarElement&&this._appendInkBarElement())}activateInkBar(e){let i=this._elementRef.nativeElement;if(!e||!i.getBoundingClientRect||!this._inkBarContentElement){i.classList.add(Sb);return}let a=i.getBoundingClientRect(),o=e.width/a.width,r=e.left-a.left;i.classList.add(Ew),this._inkBarContentElement.style.setProperty(`transform`,`translateX(${r}px) scaleX(${o})`),i.getBoundingClientRect(),i.classList.remove(Ew),i.classList.add(Sb),this._inkBarContentElement.style.setProperty(`transform`,``)}deactivateInkBar(){this._elementRef.nativeElement.classList.remove(Sb)}ngOnInit(){this._createInkBarElement()}ngOnDestroy(){this._inkBarElement?.remove(),this._inkBarElement=this._inkBarContentElement=null}_createInkBarElement(){let e=this._elementRef.nativeElement.ownerDocument||document,i=this._inkBarElement=e.createElement(`span`),a=this._inkBarContentElement=e.createElement(`span`);i.className=`mdc-tab-indicator`,a.className=`mdc-tab-indicator__content mdc-tab-indicator__content--underline`,i.appendChild(this._inkBarContentElement),this._appendInkBarElement()}_appendInkBarElement(){this._inkBarElement;(this._fitToContent?this._elementRef.nativeElement.querySelector(`.mdc-tab__content`):this._elementRef.nativeElement).appendChild(this._inkBarElement)}static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,inputs:{fitInkBarToContent:[2,`fitInkBarToContent`,`fitInkBarToContent`,V]}})}return t})();var Nw=(()=>{class t extends iR{elementRef=f(G);disabled=!1;focus(){this.elementRef.nativeElement.focus()}getOffsetLeft(){return this.elementRef.nativeElement.offsetLeft}getOffsetWidth(){return this.elementRef.nativeElement.offsetWidth}static ɵfac=(()=>{let e;return function(a){return(e||(e=It(t)))(a||t)}})();static ɵdir=q({type:t,selectors:[[``,`matTabLabelWrapper`,``]],hostVars:3,hostBindings:function(i,a){i&2&&(ee(`aria-disabled`,!!a.disabled),W(`mat-mdc-tab-disabled`,a.disabled))},inputs:{disabled:[2,`disabled`,`disabled`,V]},features:[Fe]})}return t})();var Dw={passive:!0};var aR=650;var oR=100;function wb(t){let n=t+``;return/^[0-9]+(?:\.[0-9]+)?$/.test(n)?`${t}ms`:/^[0-9]+(?:\.[0-9]+)?(?:ms|s)$/.test(n)?n:``}var rR=(()=>{class t{_elementRef=f(G);_changeDetectorRef=f(Re);_viewportRuler=f(ui);_dir=f(pt,{optional:!0});_ngZone=f(K);_platform=f(Te);_sharedResizeObserver=f(qu);_injector=f(re);_renderer=f(Ae);_animationsDisabled=ze();_eventCleanups;_scrollDistance=0;_selectedIndexChanged=!1;_destroyed=new L;_showPaginationControls=!1;_disableScrollAfter=!0;_disableScrollBefore=!0;_tabLabelCount;_scrollDistanceChanged=!1;_keyManager;_currentTextContent;_stopScrolling=new L;disablePagination=!1;get selectedIndex(){return this._selectedIndex}set selectedIndex(e){let i=isNaN(e)?0:e;this._selectedIndex!=i&&(this._selectedIndexChanged=!0,this._selectedIndex=i,this._keyManager&&this._keyManager.updateActiveItem(i))}_selectedIndex=0;selectFocusedIndex=new J;indexFocused=new J;constructor(){this._eventCleanups=this._ngZone.runOutsideAngular(()=>[this._renderer.listen(this._elementRef.nativeElement,`mouseleave`,()=>this._stopInterval())])}ngAfterViewInit(){this._eventCleanups.push(this._renderer.listen(this._previousPaginator.nativeElement,`touchstart`,()=>this._handlePaginatorPress(`before`),Dw),this._renderer.listen(this._nextPaginator.nativeElement,`touchstart`,()=>this._handlePaginatorPress(`after`),Dw))}ngAfterContentInit(){let e=this._dir?this._dir.change:In(`ltr`),i=this._sharedResizeObserver.observe(this._elementRef.nativeElement).pipe(Ri(32),Me(this._destroyed)),a=this._viewportRuler.change(150).pipe(Me(this._destroyed)),o=()=>{this.updatePagination(),this._alignInkBarToSelectedTab()};this._keyManager=new Ji(this._items).withHorizontalOrientation(this._getLayoutDirection()).withHomeAndEnd().withWrap().skipPredicate(()=>!1),this._keyManager.updateActiveItem(Math.max(this._selectedIndex,0)),Mt(o,{injector:this._injector}),wt(e,a,i,this._items.changes,this._itemsResized()).pipe(Me(this._destroyed)).subscribe(()=>{this._ngZone.run(()=>{Promise.resolve().then(()=>{this._scrollDistance=Math.max(0,Math.min(this._getMaxScrollDistance(),this._scrollDistance)),o()})}),this._keyManager?.withHorizontalOrientation(this._getLayoutDirection())}),this._keyManager.change.subscribe(r=>{this.indexFocused.emit(r),this._setTabFocus(r)})}_itemsResized(){return typeof ResizeObserver!=`function`?Oi:this._items.changes.pipe(dt(this._items),fn(e=>new xe(i=>this._ngZone.runOutsideAngular(()=>{let a=new ResizeObserver(o=>i.next(o));return e.forEach(o=>a.observe(o.elementRef.nativeElement)),()=>{a.disconnect()}}))),Ka(1),Ke(e=>e.some(i=>i.contentRect.width>0&&i.contentRect.height>0)))}ngAfterContentChecked(){this._tabLabelCount!=this._items.length&&(this.updatePagination(),this._tabLabelCount=this._items.length,this._changeDetectorRef.markForCheck()),this._selectedIndexChanged&&(this._scrollToLabel(this._selectedIndex),this._checkScrollingControls(),this._alignInkBarToSelectedTab(),this._selectedIndexChanged=!1,this._changeDetectorRef.markForCheck()),this._scrollDistanceChanged&&(this._updateTabScrollPosition(),this._scrollDistanceChanged=!1,this._changeDetectorRef.markForCheck())}ngOnDestroy(){this._eventCleanups.forEach(e=>e()),this._keyManager?.destroy(),this._destroyed.next(),this._destroyed.complete(),this._stopScrolling.complete()}_handleKeydown(e){if(!vt(e))switch(e.keyCode){case 13:case 32:if(this.focusIndex!==this.selectedIndex){let i=this._items.get(this.focusIndex);i&&!i.disabled&&(this.selectFocusedIndex.emit(this.focusIndex),this._itemSelected(e))}break;default:this._keyManager?.onKeydown(e)}}_onContentChanges(){let e=this._elementRef.nativeElement.textContent;e!==this._currentTextContent&&(this._currentTextContent=e||``,this._ngZone.run(()=>{this.updatePagination(),this._alignInkBarToSelectedTab(),this._changeDetectorRef.markForCheck()}))}updatePagination(){this._checkPaginationEnabled(),this._checkScrollingControls(),this._updateTabScrollPosition()}get focusIndex(){return this._keyManager?this._keyManager.activeItemIndex:0}set focusIndex(e){!this._isValidIndex(e)||this.focusIndex===e||!this._keyManager||this._keyManager.setActiveItem(e)}_isValidIndex(e){return this._items?!!this._items.toArray()[e]:!0}_setTabFocus(e){if(this._showPaginationControls&&this._scrollToLabel(e),this._items&&this._items.length){this._items.toArray()[e].focus();let i=this._tabListContainer.nativeElement;this._getLayoutDirection()==`ltr`?i.scrollLeft=0:i.scrollLeft=i.scrollWidth-i.offsetWidth}}_getLayoutDirection(){return this._dir&&this._dir.value===`rtl`?`rtl`:`ltr`}_updateTabScrollPosition(){if(this.disablePagination)return;let e=this.scrollDistance,i=this._getLayoutDirection()===`ltr`?-e:e;this._tabList.nativeElement.style.transform=`translateX(${Math.round(i)}px)`,(this._platform.TRIDENT||this._platform.EDGE)&&(this._tabListContainer.nativeElement.scrollLeft=0)}get scrollDistance(){return this._scrollDistance}set scrollDistance(e){this._scrollTo(e)}_scrollHeader(e){let i=this._tabListContainer.nativeElement.offsetWidth,a=(e==`before`?-1:1)*i/3;return this._scrollTo(this._scrollDistance+a)}_handlePaginatorClick(e){this._stopInterval(),this._scrollHeader(e)}_scrollToLabel(e){if(this.disablePagination)return;let i=this._items?this._items.toArray()[e]:null;if(!i)return;let a=this._tabListContainer.nativeElement.offsetWidth,{offsetLeft:o,offsetWidth:r}=i.elementRef.nativeElement,s,c;this._getLayoutDirection()==`ltr`?(s=o,c=s+r):(c=this._tabListInner.nativeElement.offsetWidth-o,s=c-r);let m=this.scrollDistance,u=this.scrollDistance+a;s<m?this.scrollDistance-=m-s:c>u&&(this.scrollDistance+=Math.min(c-u,s-m))}_checkPaginationEnabled(){if(this.disablePagination)this._showPaginationControls=!1;else{let a=this._tabListInner.nativeElement.scrollWidth-this._elementRef.nativeElement.offsetWidth>=5;a||(this.scrollDistance=0),a!==this._showPaginationControls&&(this._showPaginationControls=a,this._changeDetectorRef.markForCheck())}}_checkScrollingControls(){this.disablePagination?this._disableScrollAfter=this._disableScrollBefore=!0:(this._disableScrollBefore=this.scrollDistance==0,this._disableScrollAfter=this.scrollDistance==this._getMaxScrollDistance(),this._changeDetectorRef.markForCheck())}_getMaxScrollDistance(){return this._tabListInner.nativeElement.scrollWidth-this._tabListContainer.nativeElement.offsetWidth||0}_alignInkBarToSelectedTab(){let e=this._items&&this._items.length?this._items.toArray()[this.selectedIndex]:null,i=e?e.elementRef.nativeElement:null;i?this._inkBar.alignToElement(i):this._inkBar.hide()}_stopInterval(){this._stopScrolling.next()}_handlePaginatorPress(e,i){i&&i.button!=null&&i.button!==0||(this._stopInterval(),gs(aR,oR).pipe(Me(wt(this._stopScrolling,this._destroyed))).subscribe(()=>{let{maxScrollDistance:a,distance:o}=this._scrollHeader(e);(o===0||o>=a)&&this._stopInterval()}))}_scrollTo(e){if(this.disablePagination)return{maxScrollDistance:0,distance:0};let i=this._getMaxScrollDistance();return this._scrollDistance=Math.max(0,Math.min(i,e)),this._scrollDistanceChanged=!0,this._checkScrollingControls(),{maxScrollDistance:i,distance:this._scrollDistance}}static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,inputs:{disablePagination:[2,`disablePagination`,`disablePagination`,V],selectedIndex:[2,`selectedIndex`,`selectedIndex`,st]},outputs:{selectFocusedIndex:`selectFocusedIndex`,indexFocused:`indexFocused`}})}return t})();var sR=(()=>{class t extends rR{_items;_tabListContainer;_tabList;_tabListInner;_nextPaginator;_previousPaginator;_inkBar;ariaLabel;ariaLabelledby;disableRipple=!1;ngAfterContentInit(){this._inkBar=new Eb(this._items),super.ngAfterContentInit()}_itemSelected(e){e.preventDefault()}static ɵfac=(()=>{let e;return function(a){return(e||(e=It(t)))(a||t)}})();static ɵcmp=(function(){let e=[`tabListContainer`],i=[`tabList`],a=[`tabListInner`],o=[`nextPaginator`],r=[`previousPaginator`];return B({type:t,selectors:[[`mat-tab-header`]],contentQueries:function(m,u,g){if(m&1&&Vt(g,Nw,4),m&2){let v;X(v=Z())&&(u._items=v)}},viewQuery:function(m,u){if(m&1&&Xe(e,7)(i,7)(a,7)(o,5)(r,5),m&2){let g;X(g=Z())&&(u._tabListContainer=g.first),X(g=Z())&&(u._tabList=g.first),X(g=Z())&&(u._tabListInner=g.first),X(g=Z())&&(u._nextPaginator=g.first),X(g=Z())&&(u._previousPaginator=g.first)}},hostAttrs:[1,`mat-mdc-tab-header`],hostVars:4,hostBindings:function(m,u){m&2&&W(`mat-mdc-tab-header-pagination-controls-enabled`,u._showPaginationControls)(`mat-mdc-tab-header-rtl`,u._getLayoutDirection()==`rtl`)},inputs:{ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],disableRipple:[2,`disableRipple`,`disableRipple`,V]},features:[Fe],ngContentSelectors:[`*`],decls:13,vars:10,consts:[[`previousPaginator`,``],[`tabListContainer`,``],[`tabList`,``],[`tabListInner`,``],[`nextPaginator`,``],[`mat-ripple`,``,1,`mat-mdc-tab-header-pagination`,`mat-mdc-tab-header-pagination-before`,3,`click`,`mousedown`,`touchend`,`matRippleDisabled`],[1,`mat-mdc-tab-header-pagination-chevron`],[1,`mat-mdc-tab-label-container`,3,`keydown`],[`role`,`tablist`,1,`mat-mdc-tab-list`,3,`cdkObserveContent`],[1,`mat-mdc-tab-labels`],[`mat-ripple`,``,1,`mat-mdc-tab-header-pagination`,`mat-mdc-tab-header-pagination-after`,3,`mousedown`,`click`,`touchend`,`matRippleDisabled`]],template:function(m,u){m&1&&(Oe(),l(0,`div`,5,0),C(`click`,function(){return u._handlePaginatorClick(`before`)})(`mousedown`,function(v){return u._handlePaginatorPress(`before`,v)})(`touchend`,function(){return u._stopInterval()}),z(2,`div`,6),d(),l(3,`div`,7,1),C(`keydown`,function(v){return u._handleKeydown(v)}),l(5,`div`,8,2),C(`cdkObserveContent`,function(){return u._onContentChanges()}),l(7,`div`,9,3),ue(9),d()()(),l(10,`div`,10,4),C(`mousedown`,function(v){return u._handlePaginatorPress(`after`,v)})(`click`,function(){return u._handlePaginatorClick(`after`)})(`touchend`,function(){return u._stopInterval()}),z(12,`div`,6),d()),m&2&&(W(`mat-mdc-tab-header-pagination-disabled`,u._disableScrollBefore),S(`matRippleDisabled`,u._disableScrollBefore||u.disableRipple),h(3),W(`_mat-animation-noopable`,u._animationsDisabled),h(2),ee(`aria-label`,u.ariaLabel||null)(`aria-labelledby`,u.ariaLabelledby||null),h(5),W(`mat-mdc-tab-header-pagination-disabled`,u._disableScrollAfter),S(`matRippleDisabled`,u._disableScrollAfter||u.disableRipple))},dependencies:[ci,TC],styles:[`.mat-mdc-tab-header {
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
`],encapsulation:2,changeDetection:1})})()}return t})();var cR=new D(`MAT_TABS_CONFIG`);var kw=(()=>{class t extends Ta{_host=f(Db);_ngZone=f(K);_centeringSub=De.EMPTY;_leavingSub=De.EMPTY;ngOnInit(){super.ngOnInit(),this._centeringSub=this._host._beforeCentering.pipe(dt(this._host._isCenterPosition())).subscribe(e=>{this._host._content&&e&&!this.hasAttached()&&this._ngZone.run(()=>{Promise.resolve().then(),this.attach(this._host._content)})}),this._leavingSub=this._host._afterLeavingCenter.subscribe(()=>{this._host.preserveContent||this._ngZone.run(()=>this.detach())})}ngOnDestroy(){super.ngOnDestroy(),this._centeringSub.unsubscribe(),this._leavingSub.unsubscribe()}static ɵfac=(()=>{let e;return function(a){return(e||(e=It(t)))(a||t)}})();static ɵdir=q({type:t,selectors:[[``,`matTabBodyHost`,``]],features:[Fe]})}return t})();var Db=(()=>{class t{_elementRef=f(G);_dir=f(pt,{optional:!0});_ngZone=f(K);_injector=f(re);_renderer=f(Ae);_diAnimationsDisabled=ze();_eventCleanups;_initialized=!1;_fallbackTimer;_positionIndex;_dirChangeSubscription=De.EMPTY;_position;_previousPosition;_onCentering=new J;_beforeCentering=new J;_afterLeavingCenter=new J;_onCentered=new J(!0);_portalHost;_contentElement;_content;animationDuration=`500ms`;preserveContent=!1;set position(e){this._positionIndex=e,this._computePositionAnimationState()}constructor(){if(this._dir){let e=f(Re);this._dirChangeSubscription=this._dir.change.subscribe(i=>{this._computePositionAnimationState(i),e.markForCheck()})}}ngOnInit(){this._bindTransitionEvents(),this._position===`center`&&(this._setActiveClass(!0),Mt(()=>this._onCentering.emit(this._elementRef.nativeElement.clientHeight),{injector:this._injector})),this._initialized=!0}ngOnDestroy(){clearTimeout(this._fallbackTimer),this._eventCleanups?.forEach(e=>e()),this._dirChangeSubscription.unsubscribe()}_bindTransitionEvents(){this._ngZone.runOutsideAngular(()=>{let e=this._elementRef.nativeElement,i=a=>{a.target===this._contentElement?.nativeElement&&(this._elementRef.nativeElement.classList.remove(`mat-tab-body-animating`),a.type===`transitionend`&&this._transitionDone())};this._eventCleanups=[this._renderer.listen(e,`transitionstart`,a=>{a.target===this._contentElement?.nativeElement&&(this._elementRef.nativeElement.classList.add(`mat-tab-body-animating`),this._transitionStarted())}),this._renderer.listen(e,`transitionend`,i),this._renderer.listen(e,`transitioncancel`,i)]})}_transitionStarted(){clearTimeout(this._fallbackTimer);let e=this._position===`center`;this._beforeCentering.emit(e),e&&this._onCentering.emit(this._elementRef.nativeElement.clientHeight)}_transitionDone(){this._position===`center`?this._onCentered.emit():this._previousPosition===`center`&&this._afterLeavingCenter.emit()}_setActiveClass(e){this._elementRef.nativeElement.classList.toggle(`mat-mdc-tab-body-active`,e)}_getLayoutDirection(){return this._dir&&this._dir.value===`rtl`?`rtl`:`ltr`}_isCenterPosition(){return this._positionIndex===0}_computePositionAnimationState(e=this._getLayoutDirection()){this._previousPosition=this._position,this._positionIndex<0?this._position=e==`ltr`?`left`:`right`:this._positionIndex>0?this._position=e==`ltr`?`right`:`left`:this._position=`center`,this._animationsDisabled()?this._simulateTransitionEvents():this._initialized&&(this._position===`center`||this._previousPosition===`center`)&&(clearTimeout(this._fallbackTimer),this._fallbackTimer=this._ngZone.runOutsideAngular(()=>setTimeout(()=>this._simulateTransitionEvents(),100)))}_simulateTransitionEvents(){this._transitionStarted(),Mt(()=>this._transitionDone(),{injector:this._injector})}_animationsDisabled(){return this._diAnimationsDisabled||this.animationDuration===`0ms`||this.animationDuration===`0s`}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`content`];function i(a,o){}return B({type:t,selectors:[[`mat-tab-body`]],viewQuery:function(o,r){if(o&1&&Xe(kw,5)(e,5),o&2){let s;X(s=Z())&&(r._portalHost=s.first),X(s=Z())&&(r._contentElement=s.first)}},hostAttrs:[1,`mat-mdc-tab-body`],hostVars:1,hostBindings:function(o,r){o&2&&ee(`inert`,r._position===`center`?null:``)},inputs:{_content:[0,`content`,`_content`],animationDuration:`animationDuration`,preserveContent:`preserveContent`,position:`position`},outputs:{_onCentering:`_onCentering`,_beforeCentering:`_beforeCentering`,_onCentered:`_onCentered`},decls:3,vars:6,consts:[[`content`,``],[`cdkScrollable`,``,1,`mat-mdc-tab-body-content`],[`matTabBodyHost`,``]],template:function(o,r){o&1&&(l(0,`div`,1,0),at(2,i,0,0,`ng-template`,2),d()),o&2&&W(`mat-tab-body-content-left`,r._position===`left`)(`mat-tab-body-content-right`,r._position===`right`)(`mat-tab-body-content-can-animate`,r._position===`center`||r._previousPosition===`center`)},dependencies:[kw,zc],styles:[`.mat-mdc-tab-body {
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
`],encapsulation:2,changeDetection:1})})()}return t})();var lm=(()=>{class t{_elementRef=f(G);_changeDetectorRef=f(Re);_ngZone=f(K);_tabsSubscription=De.EMPTY;_tabLabelSubscription=De.EMPTY;_tabBodySubscription=De.EMPTY;_diAnimationsDisabled=ze();_bodyAnimationDuration;_headerAnimationDuration;_allTabs;_tabBodies;_tabBodyWrapper;_tabHeader;_tabs=new _n;_indexToSelect=0;_lastFocusedTabIndex=null;_tabBodyWrapperHeight=0;color;get fitInkBarToContent(){return this._fitInkBarToContent}set fitInkBarToContent(e){this._fitInkBarToContent=e,this._changeDetectorRef.markForCheck()}_fitInkBarToContent=!1;stretchTabs=!0;alignTabs=null;dynamicHeight=!1;get selectedIndex(){return this._selectedIndex}set selectedIndex(e){this._indexToSelect=isNaN(e)?null:e}_selectedIndex=null;headerPosition=`above`;get animationDuration(){return this._animationDuration}set animationDuration(e){this._animationDuration=e,e&&typeof e==`object`?(this._bodyAnimationDuration=wb(e.body),this._headerAnimationDuration=wb(e.header)):this._headerAnimationDuration=this._bodyAnimationDuration=wb(e)}_animationDuration;get contentTabIndex(){return this._contentTabIndex}set contentTabIndex(e){this._contentTabIndex=isNaN(e)?null:e}_contentTabIndex=null;disablePagination=!1;disableRipple=!1;preserveContent=!1;get backgroundColor(){return this._backgroundColor}set backgroundColor(e){let i=this._elementRef.nativeElement.classList;i.remove(`mat-tabs-with-background`,`mat-background-${this.backgroundColor}`),e&&i.add(`mat-tabs-with-background`,`mat-background-${e}`),this._backgroundColor=e}_backgroundColor;ariaLabel;ariaLabelledby;selectedIndexChange=new J;focusChange=new J;animationDone=new J;selectedTabChange=new J(!0);_groupId;_isServer=!f(Te).isBrowser;constructor(){let e=f(cR,{optional:!0});this._groupId=f(Le).getId(`mat-tab-group-`),this.animationDuration=e&&e.animationDuration?e.animationDuration:`500ms`,this.disablePagination=e&&e.disablePagination!=null?e.disablePagination:!1,this.dynamicHeight=e&&e.dynamicHeight!=null?e.dynamicHeight:!1,e?.contentTabIndex!=null&&(this.contentTabIndex=e.contentTabIndex),this.preserveContent=!!e?.preserveContent,this.fitInkBarToContent=e&&e.fitInkBarToContent!=null?e.fitInkBarToContent:!1,this.stretchTabs=e&&e.stretchTabs!=null?e.stretchTabs:!0,this.alignTabs=e&&e.alignTabs!=null?e.alignTabs:null}ngAfterContentChecked(){let e=this._indexToSelect=this._clampTabIndex(this._indexToSelect);if(this._selectedIndex!=e){let i=this._selectedIndex==null;if(!i){this.selectedTabChange.emit(this._createChangeEvent(e));let a=this._tabBodyWrapper.nativeElement;a.style.minHeight=a.clientHeight+`px`}Promise.resolve().then(()=>{this._tabs.forEach((a,o)=>a.isActive=o===e),i||(this.selectedIndexChange.emit(e),this._tabBodyWrapper.nativeElement.style.minHeight=``)})}this._tabs.forEach((i,a)=>{i.position=a-e,this._selectedIndex!=null&&i.position==0&&!i.origin&&(i.origin=e-this._selectedIndex)}),this._selectedIndex!==e&&(this._selectedIndex=e,this._lastFocusedTabIndex=null,this._changeDetectorRef.markForCheck())}ngAfterContentInit(){this._subscribeToAllTabChanges(),this._subscribeToTabLabels(),this._tabsSubscription=this._tabs.changes.subscribe(()=>{let e=this._clampTabIndex(this._indexToSelect);if(e===this._selectedIndex){let i=this._tabs.toArray(),a;for(let o=0;o<i.length;o++)if(i[o].isActive){this._indexToSelect=this._selectedIndex=o,this._lastFocusedTabIndex=null,a=i[o];break}!a&&i[e]&&Promise.resolve().then(()=>{i[e].isActive=!0,this.selectedTabChange.emit(this._createChangeEvent(e))})}this._changeDetectorRef.markForCheck()})}ngAfterViewInit(){this._tabBodySubscription=this._tabBodies.changes.subscribe(()=>this._bodyCentered(!0))}_subscribeToAllTabChanges(){this._allTabs.changes.pipe(dt(this._allTabs)).subscribe(e=>{this._tabs.reset(e.filter(i=>i._closestTabGroup===this||!i._closestTabGroup)),this._tabs.notifyOnChanges()})}ngOnDestroy(){this._tabs.destroy(),this._tabsSubscription.unsubscribe(),this._tabLabelSubscription.unsubscribe(),this._tabBodySubscription.unsubscribe()}realignInkBar(){this._tabHeader&&this._tabHeader._alignInkBarToSelectedTab()}updatePagination(){this._tabHeader&&this._tabHeader.updatePagination()}focusTab(e){let i=this._tabHeader;i&&(i.focusIndex=e)}_focusChanged(e){this._lastFocusedTabIndex=e,this.focusChange.emit(this._createChangeEvent(e))}_createChangeEvent(e){let i=new kb;return i.index=e,this._tabs&&this._tabs.length&&(i.tab=this._tabs.toArray()[e]),i}_subscribeToTabLabels(){this._tabLabelSubscription&&this._tabLabelSubscription.unsubscribe(),this._tabLabelSubscription=wt(...this._tabs.map(e=>e._stateChanges)).subscribe(()=>this._changeDetectorRef.markForCheck())}_clampTabIndex(e){return Math.min(this._tabs.length-1,Math.max(e||0,0))}_getTabLabelId(e,i){return e.id||`${this._groupId}-label-${i}`}_getTabContentId(e){return`${this._groupId}-content-${e}`}_setTabBodyWrapperHeight(e){if(!this.dynamicHeight||!this._tabBodyWrapperHeight){this._tabBodyWrapperHeight=e;return}let i=this._tabBodyWrapper.nativeElement;i.style.height=this._tabBodyWrapperHeight+`px`,this._tabBodyWrapper.nativeElement.offsetHeight&&(i.style.height=e+`px`)}_removeTabBodyWrapperHeight(){let e=this._tabBodyWrapper.nativeElement;this._tabBodyWrapperHeight=e.clientHeight,e.style.height=``,this._ngZone.run(()=>this.animationDone.emit())}_handleClick(e,i,a){i.focusIndex=a,e.disabled||(this.selectedIndex=a)}_getTabIndex(e){return e===(this._lastFocusedTabIndex??this.selectedIndex)?0:-1}_tabFocusChanged(e,i){e&&e!==`mouse`&&e!==`touch`&&(this._tabHeader.focusIndex=i)}_bodyCentered(e){e&&this._tabBodies?.forEach((i,a)=>i._setActiveClass(a===this._selectedIndex))}_bodyAnimationsDisabled(){return this._diAnimationsDisabled||this._bodyAnimationDuration===`0`||this._bodyAnimationDuration===`0ms`}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`tabBodyWrapper`],i=[`tabHeader`],a=[`*`];function o(g,v){}function r(g,v){if(g&1&&at(0,o,0,0,`ng-template`,12),g&2){let b=_().$implicit;S(`cdkPortalOutlet`,b.templateLabel)}}function s(g,v){if(g&1&&p(0),g&2){let b=_().$implicit;w(b.textLabel)}}function c(g,v){if(g&1){let b=ae();l(0,`div`,7,2),C(`click`,function(){let E=A(b),R=E.$implicit,oe=E.$index,j=_(),x=mt(1);return O(j._handleClick(R,x,oe))})(`cdkFocusChange`,function(E){let R=A(b).$index;return O(_()._tabFocusChanged(E,R))}),z(2,`span`,8)(3,`div`,9),l(4,`span`,10)(5,`span`,11),N(6,r,1,1,null,12)(7,s,1,1),d()()()}if(g&2){let b=v.$implicit,y=v.$index,E=mt(1),R=_();et(b.labelClass),W(`mdc-tab--active`,R.selectedIndex===y),S(`id`,R._getTabLabelId(b,y))(`disabled`,b.disabled)(`fitInkBarToContent`,R.fitInkBarToContent),ee(`tabIndex`,R._getTabIndex(y))(`aria-posinset`,y+1)(`aria-setsize`,R._tabs.length)(`aria-controls`,R._getTabContentId(y))(`aria-selected`,R.selectedIndex===y)(`aria-label`,b.ariaLabel||null)(`aria-labelledby`,!b.ariaLabel&&b.ariaLabelledby?b.ariaLabelledby:null),h(3),S(`matRippleTrigger`,E)(`matRippleDisabled`,b.disabled||R.disableRipple),h(3),T(b.templateLabel?6:7)}}function m(g,v){g&1&&ue(0)}function u(g,v){if(g&1){let b=ae();l(0,`mat-tab-body`,13),C(`_onCentered`,function(){A(b);return O(_()._removeTabBodyWrapperHeight())})(`_onCentering`,function(E){A(b);return O(_()._setTabBodyWrapperHeight(E))})(`_beforeCentering`,function(E){A(b);return O(_()._bodyCentered(E))}),d()}if(g&2){let b=v.$implicit,y=v.$index,E=_();et(b.bodyClass),S(`id`,E._getTabContentId(y))(`content`,b.content)(`position`,b.position)(`animationDuration`,E._bodyAnimationDuration)(`preserveContent`,E.preserveContent),ee(`tabindex`,E.contentTabIndex!=null&&E.selectedIndex===y?E.contentTabIndex:null)(`aria-labelledby`,E._getTabLabelId(b,y))(`aria-hidden`,E.selectedIndex!==y)}}return B({type:t,selectors:[[`mat-tab-group`]],contentQueries:function(v,b,y){if(v&1&&Vt(y,Wc,5),v&2){let E;X(E=Z())&&(b._allTabs=E)}},viewQuery:function(v,b){if(v&1&&Xe(e,5)(i,5)(Db,5),v&2){let y;X(y=Z())&&(b._tabBodyWrapper=y.first),X(y=Z())&&(b._tabHeader=y.first),X(y=Z())&&(b._tabBodies=y)}},hostAttrs:[1,`mat-mdc-tab-group`],hostVars:13,hostBindings:function(v,b){v&2&&(ee(`mat-align-tabs`,b.alignTabs),et(`mat-`+(b.color||`primary`)),mn(`--%NS%mat-tab-body-animation-duration`,b._bodyAnimationDuration)(`--%NS%mat-tab-header-animation-duration`,b._headerAnimationDuration),W(`mat-mdc-tab-group-dynamic-height`,b.dynamicHeight)(`mat-mdc-tab-group-inverted-header`,b.headerPosition===`below`)(`mat-mdc-tab-group-stretch-tabs`,b.stretchTabs))},inputs:{color:`color`,fitInkBarToContent:[2,`fitInkBarToContent`,`fitInkBarToContent`,V],stretchTabs:[2,`mat-stretch-tabs`,`stretchTabs`,V],alignTabs:[0,`mat-align-tabs`,`alignTabs`],dynamicHeight:[2,`dynamicHeight`,`dynamicHeight`,V],selectedIndex:[2,`selectedIndex`,`selectedIndex`,st],headerPosition:`headerPosition`,animationDuration:`animationDuration`,contentTabIndex:[2,`contentTabIndex`,`contentTabIndex`,st],disablePagination:[2,`disablePagination`,`disablePagination`,V],disableRipple:[2,`disableRipple`,`disableRipple`,V],preserveContent:[2,`preserveContent`,`preserveContent`,V],backgroundColor:`backgroundColor`,ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`]},outputs:{selectedIndexChange:`selectedIndexChange`,focusChange:`focusChange`,animationDone:`animationDone`,selectedTabChange:`selectedTabChange`},exportAs:[`matTabGroup`],features:[$e([{provide:Mw,useExisting:t}])],ngContentSelectors:a,decls:9,vars:8,consts:[[`tabHeader`,``],[`tabBodyWrapper`,``],[`tabNode`,``],[3,`indexFocused`,`selectFocusedIndex`,`selectedIndex`,`disableRipple`,`disablePagination`,`aria-label`,`aria-labelledby`],[`role`,`tab`,`matTabLabelWrapper`,``,`cdkMonitorElementFocus`,``,1,`mdc-tab`,`mat-mdc-tab`,`mat-focus-indicator`,3,`id`,`mdc-tab--active`,`class`,`disabled`,`fitInkBarToContent`],[1,`mat-mdc-tab-body-wrapper`],[`role`,`tabpanel`,3,`id`,`class`,`content`,`position`,`animationDuration`,`preserveContent`],[`role`,`tab`,`matTabLabelWrapper`,``,`cdkMonitorElementFocus`,``,1,`mdc-tab`,`mat-mdc-tab`,`mat-focus-indicator`,3,`click`,`cdkFocusChange`,`id`,`disabled`,`fitInkBarToContent`],[1,`mdc-tab__ripple`],[`mat-ripple`,``,1,`mat-mdc-tab-ripple`,3,`matRippleTrigger`,`matRippleDisabled`],[1,`mdc-tab__content`],[1,`mdc-tab__text-label`],[3,`cdkPortalOutlet`],[`role`,`tabpanel`,3,`_onCentered`,`_onCentering`,`_beforeCentering`,`id`,`content`,`position`,`animationDuration`,`preserveContent`]],template:function(v,b){v&1&&(Oe(),l(0,`mat-tab-header`,3,0),C(`indexFocused`,function(E){return b._focusChanged(E)})(`selectFocusedIndex`,function(E){return b.selectedIndex=E}),ve(2,c,8,17,`div`,4,ii),d(),N(4,m,1,0),l(5,`div`,5,1),ve(7,u,1,10,`mat-tab-body`,6,ii),d()),v&2&&(S(`selectedIndex`,b.selectedIndex||0)(`disableRipple`,b.disableRipple)(`disablePagination`,b.disablePagination),Er(`aria-label`,b.ariaLabel)(`aria-labelledby`,b.ariaLabelledby),h(2),ye(b._tabs),h(2),T(b._isServer?4:-1),h(),W(`_mat-animation-noopable`,b._bodyAnimationsDisabled()),h(2),ye(b._tabs))},dependencies:[sR,Nw,Mg,ci,Ta,Db],styles:[`.mdc-tab {
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
`],encapsulation:2,changeDetection:1})})()}return t})();var kb=class{index;tab};var dm=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({imports:[we]})}return t})();var lR=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,selectors:[[`mat-toolbar-row`]],hostAttrs:[1,`mat-toolbar-row`],exportAs:[`matToolbarRow`]})}return t})();var um=(()=>{class t{_elementRef=f(G);_platform=f(Te);_document=f(ce);color;_toolbarRows;ngAfterViewInit(){this._platform.isBrowser&&(this._checkToolbarMixedModes(),this._toolbarRows.changes.subscribe(()=>this._checkToolbarMixedModes()))}_checkToolbarMixedModes(){this._toolbarRows.length}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`*`,[[`mat-toolbar-row`]]];return B({type:t,selectors:[[`mat-toolbar`]],contentQueries:function(o,r,s){if(o&1&&Vt(s,lR,5),o&2){let c;X(c=Z())&&(r._toolbarRows=c)}},hostAttrs:[1,`mat-toolbar`],hostVars:6,hostBindings:function(o,r){o&2&&(et(r.color?`mat-`+r.color:``),W(`mat-toolbar-multiple-rows`,r._toolbarRows.length>0)(`mat-toolbar-single-row`,r._toolbarRows.length===0))},inputs:{color:`color`},exportAs:[`matToolbar`],ngContentSelectors:[`*`,`mat-toolbar-row`],decls:2,vars:0,template:function(o,r){o&1&&(Oe(e),ue(0),ue(1,1))},styles:[`.mat-toolbar {
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
`],encapsulation:2})})()}return t})();var mm=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({imports:[we]})}return t})();function En(t){t||(t=f(Ne));let n=new xe(e=>{if(t.destroyed){e.next();return}return t.onDestroy(e.next.bind(e))});return e=>e.pipe(Me(n))}var Pw=v(Rw());var Dn={apiBaseUrl:``,websocketUrl:`${window.location.protocol===`https:`?`wss:`:`ws:`}//${window.location.host}/websocket`,websocketDebug:!1};var os=class t{http=f(Ei);destroyRef=f(Ne);envelopeSubject=new L;connectedSubject=new L;disconnectedSubject=new L;errorSubject=new L;stateSubject=new hi(`connecting`);pending=new Map;client=null;subscription=null;retryTimer=null;connected=!1;synchronization=null;again=!1;generation=0;delay=500;logId=null;cursor=-1;envelopes$=this.envelopeSubject.asObservable();connected$=this.connectedSubject.asObservable();disconnected$=this.disconnectedSubject.asObservable();errors$=this.errorSubject.asObservable();state$=this.stateSubject.asObservable();get lastEventId(){return this.cursor}get eventLogId(){return this.logId}constructor(){this.destroyRef.onDestroy(()=>{this.stop()})}start(){if(this.client)return;let n=new Pw.Client({brokerURL:Dn.websocketUrl,heartbeatIncoming:4e3,heartbeatOutgoing:4e3,reconnectDelay:1e3,debug:Dn.websocketDebug?e=>console.debug(`[STOMP] `+e):()=>{}});n.onConnect=()=>{this.subscription?.unsubscribe(),this.subscription=n.subscribe(`/topic/events`,e=>this.receiveMessage(e)),this.connected=!0,this.generation++,this.connectedSubject.next(),this.retryNow()},n.onWebSocketClose=()=>{this.subscription=null,this.connected=!1,this.generation++,this.stateSubject.next(`disconnected`),this.disconnectedSubject.next(),this.clearTimer()},n.onWebSocketError=e=>this.fail(e),n.onStompError=e=>this.fail(e),this.client=n,n.activate()}async stop(){this.connected=!1,this.generation++,this.clearTimer(),this.pending.clear(),this.subscription?.unsubscribe(),this.subscription=null;let n=this.client;this.client=null,n&&await n.deactivate()}retryNow(){this.clearTimer(),this.delay=500,this.requestSync()}getHead(){return At(this.http.get(Dn.apiBaseUrl+`/lastevent`).pipe(En(this.destroyRef)))}getEvents(n,e,i){return At(this.http.get(Dn.apiBaseUrl+`/events`,{params:{eventLogId:n,from:e,to:i}}).pipe(En(this.destroyRef)))}receiveMessage(n){try{let e=JSON.parse(n.body);if(this.validate(e),e.eventLogId===this.logId&&e.eventId<=this.cursor)return;this.pending.size>=5e3&&(this.pending.clear(),this.requestSync()),this.pending.set(this.key(e.eventLogId,e.eventId),e),this.stateSubject.value===`live`&&e.eventLogId===this.logId&&this.drain(),(e.eventLogId!==this.logId||this.hasGap())&&this.requestSync()}catch(e){this.fail(e)}}requestSync(){if(!this.connected)return;if(this.synchronization){this.again=!0;return}this.again=!1;let n=this.generation;this.synchronization=this.synchronize(n).catch(e=>{n===this.generation&&this.connected&&this.fail(e)}).finally(()=>{this.synchronization=null,this.again&&this.connected&&this.requestSync()})}async synchronize(n){this.stateSubject.next(`syncing`);let e=await this.getHead();if(this.current(n)){if(typeof e.eventLogId!=`string`||!Number.isInteger(e.lastEventId)||e.lastEventId<-1)throw new Error(`Invalid event head`);if(this.logId!==e.eventLogId){this.logId=e.eventLogId,this.cursor=e.lastEventId;for(let[i,a]of this.pending)(a.eventLogId!==this.logId||a.eventId<=this.cursor)&&this.pending.delete(i)}else{if(e.lastEventId<this.cursor)throw new Error(`Cursor moved backwards without a new log identity`);await this.download(this.cursor+1,e.lastEventId+1,n)}if(this.current(n)){this.drain();for(let i=0;this.hasGap();i++){if(i===10)throw new Error(`Live delivery has not caught up`);let a=this.smallestPending();if(a===null)break;if(await this.download(this.cursor+1,a,n),!this.current(n))return;this.drain()}if(this.cursor<e.lastEventId)throw new Error(`Incomplete event history`);this.current(n)&&(this.delay=500,this.clearTimer(),this.stateSubject.next(`live`))}}}async download(n,e,i){for(let a=n;a<e;){let o=Math.min(e,a+1e3),r=this.logId,s=await this.getEvents(r,a,o);if(!this.current(i))return;if(s.eventLogId!==r||s.from!==a||s.to!==o||s.events.length!==o-a)throw new Error(`Incomplete or obsolete history`);let c=a;for(let m of s.events){if(this.validate(m),m.eventLogId!==r||m.eventId!==c++)throw new Error(`Non-contiguous history`);m.eventId>this.cursor&&this.pending.set(this.key(r,m.eventId),m)}this.drain(),a=o}}drain(){if(this.logId)for(let n=this.pending.get(this.key(this.logId,this.cursor+1));n;n=this.pending.get(this.key(this.logId,this.cursor+1)))this.pending.delete(this.key(this.logId,n.eventId)),this.cursor=n.eventId,this.envelopeSubject.next(n)}smallestPending(){let n=null;for(let e of this.pending.values())e.eventLogId===this.logId&&e.eventId>this.cursor&&(n===null||e.eventId<n)&&(n=e.eventId);return n}hasGap(){let n=this.smallestPending();return n!==null&&n>this.cursor+1}current(n){return this.connected&&n===this.generation}key(n,e){return n+`:`+e}clearTimer(){this.retryTimer&&clearTimeout(this.retryTimer),this.retryTimer=null}fail(n){this.errorSubject.next(n),this.stateSubject.next(`retrying`),this.connected&&!this.retryTimer&&(this.retryTimer=setTimeout(()=>{this.retryTimer=null,this.requestSync()},this.delay),this.delay=Math.min(3e4,this.delay*2))}validate(n){if(!n||typeof n.eventLogId!=`string`||!Number.isInteger(n.eventId)||n.eventId<0||typeof n.type!=`string`||!Number.isFinite(n.timestamp)||typeof n.workerName!=`string`||!n.payload||typeof n.payload!=`object`)throw new Error(`Invalid envelope`);let e=n.payload;if(n.type===`SolutionGeneratedEvent`&&(typeof e.resultId!=`string`||typeof e.success!=`boolean`||typeof e.iteration!=`string`||!e.objectives||typeof e.objectives!=`object`))throw new Error(`Invalid result event`);if(n.type===`ExecutionStartedEvent`&&(!e.objectives||typeof e.objectives!=`object`||!Array.isArray(e.experimentNames)))throw new Error(`Invalid execution event`);if(n.type===`ErrorEvent`&&typeof e.exceptionType!=`string`)throw new Error(`Invalid error event`)}static ɵfac=function(e){return new(e||t)};static ɵprov=he({token:t,factory:t.ɵfac,providedIn:`root`})};var Pa=class t{http=f(Ei);destroyRef=f(Ne);get(n,e={}){return At(this.http.get(`${Dn.apiBaseUrl}${n}`,{params:e}).pipe(En(this.destroyRef)))}getMode(){return this.get(`/api/monitor/status`)}getStatus(){return this.get(`/api/experiments/status`)}getInstances(n,e){return this.get(`/api/experiments/instances`,r({runId:n},e))}getInstance(n,e,i){return this.get(`/api/experiments/instances/${encodeURIComponent(e)}`,{runId:n,objective:i})}getResults(n,e){let i=s(r({runId:n},e),{order:e.order??`PLANNED`});return e.state||delete i.state,this.get(`/api/experiments/results`,i)}getResult(n,e){return this.get(`/api/experiments/results/${encodeURIComponent(e)}`,{runId:n})}getIssues(n,e,i){return this.get(`/api/experiments/issues`,{runId:n,severity:e,offset:i,limit:25})}getArtifacts(n){return this.get(`/api/experiments/artifacts`,{runId:n})}artifactUrl(n,e){return`${Dn.apiBaseUrl}/api/experiments/artifacts/${encodeURIComponent(e)}?${new URLSearchParams({runId:n})}`}exportUrl(n,e,i){let a=new URLSearchParams({runId:n,format:i,instanceId:e.instanceId,algorithm:e.algorithm,search:e.search,order:e.order??`PLANNED`});return e.state&&a.set(`state`,e.state),`${Dn.apiBaseUrl}/api/experiments/results/export?${a}`}static ɵfac=function(e){return new(e||t)};static ɵprov=he({token:t,factory:t.ɵfac,providedIn:`root`})};var Fa=class{static buildInstance(n,e,i,a=!0){let o=this.referenceValue(n.started.refValues,e);return{key:n.key,experimentName:n.started.experimentName,instanceName:n.started.instanceName,selectedObjective:e,referenceValue:o,scoreChart:this.buildScoreChart(n,e,o,a),convergenceChart:this.buildConvergenceChart(n,e,i,o,a),bestSolution:this.findBest(n.solutions,e,i)}}static buildScoreChart(n,e,i,a){let o=this.groupSuccessfulPoints(n,e,!1,null);return{options:this.chartOptions(`${n.started.instanceName} scores`,e,i,o,`Raw score for every successful repetition.`,a)}}static buildConvergenceChart(n,e,i,a,o){let r=this.groupSuccessfulPoints(n,e,!0,i);return{options:this.chartOptions(`${n.started.instanceName} best score across repetitions`,e,a,r,`Best score reached by each algorithm over completed repetitions.`,o)}}static groupSuccessfulPoints(n,e,i,a){if(!e)return[];let o=[...n.started.algorithms];for(let s of n.solutions)o.includes(s.event.algorithmName)||o.push(s.event.algorithmName);let r=[];for(let s of o){let c=[],m=null;for(let u of n.solutions){let g=u.event;if(g.algorithmName!==s)continue;let v=g.success?this.objectiveValue(g,e):null;if(v===null){c.push({x:u.ordinal,y:null,custom:{iteration:g.iteration}});continue}i&&(m=m===null||this.isBetter(v,m,a)?v:m),c.push({x:u.ordinal,y:i?m:v,custom:{iteration:g.iteration}})}r.push({type:`line`,name:s,data:c,lineWidth:i?2:1,marker:{enabled:!i},animation:!1,turboThreshold:0})}return r}static referenceValue(n,e){let i=e?n[e]:void 0;return typeof i==`number`&&Number.isFinite(i)?i:null}static scoreAxis(n,e,i){let a=i?e:null,o=[];return a!==null&&o.push({id:`reference`,className:`reference-line`,value:a,color:`#d32f2f`,dashStyle:`Dash`,width:2,zIndex:4,label:{text:`Reference: ${a}`}}),{title:{text:n??`Score`},plotLines:o,softMin:a??void 0,softMax:a??void 0}}static chartOptions(n,e,i,a,o,r){return{accessibility:{enabled:!0,description:o},boost:{enabled:!0,useGPUTranslations:!0,seriesThreshold:25},chart:{animation:!1,height:320,spacing:[12,12,12,12]},credits:{enabled:!1},exporting:{enabled:!0},legend:{enabled:!0},title:{text:n,style:{display:`none`}},subtitle:{text:e?`Objective: ${e}`:`Waiting for objective metadata`},xAxis:{allowDecimals:!1,min:1,title:{text:`Repetition`}},yAxis:this.scoreAxis(e,i,r),tooltip:{headerFormat:``,pointFormat:`<b>{series.name}</b><br/>Repetition: {point.x}<br/>Iteration: {point.custom.iteration}<br/>Value: {point.y}`},plotOptions:{series:{animation:!1,connectNulls:!1}},series:a}}static findBest(n,e,i){if(!e||!i)return null;let a=null,o=null;for(let r of n){if(!r.event.success)continue;let s=this.objectiveValue(r.event,e);s!==null&&(o===null||this.isBetter(s,o,i))&&(o=s,a=r.event)}return a}static objectiveValue(n,e){let i=n.objectives[e];return typeof i==`number`&&Number.isFinite(i)?i:null}static isBetter(n,e,i){return i===`MAXIMIZE`?n>e:n<e}};var rs=class{static instance(n,e,i=!0){return Fa.buildInstance({key:n.instance.id,started:{experimentName:n.instance.experimentName,instanceName:n.instance.instanceName,algorithms:n.algorithms.map(a=>a.name),repetitions:n.instance.counts.planned,refValues:n.instance.references},solutions:n.results.map(a=>({ordinal:a.ordinal,event:{resultId:a.resultId,success:a.state===`SUCCEEDED`,experimentName:a.experimentName,instanceName:a.instanceName,algorithmName:a.algorithmName,iteration:a.iteration,objectives:a.objectives,score:a.objectives[n.objective]??NaN,executionTime:a.executionTime??-1,timeToBest:a.timeToBest??-1}}))},n.objective,e,i)}static distribution(n,e=!0){return{options:{chart:{type:`boxplot`,height:320,animation:!1},plotOptions:{series:{animation:!1}},title:{text:`Score distribution`},credits:{enabled:!1},exporting:{enabled:!0},accessibility:{enabled:!0},xAxis:{categories:n.algorithms.map(i=>i.name)},yAxis:Fa.scoreAxis(n.objective,Fa.referenceValue(n.instance.references,n.objective),e),series:[{type:`boxplot`,name:n.objective,data:n.algorithms.flatMap((i,a)=>{let o=i.objective;return o.best===null||o.worst===null?[]:[{x:a,low:Math.min(o.best,o.worst),q1:o.lowerQuartile,median:o.median,q3:o.upperQuartile,high:Math.max(o.best,o.worst)}]})}]}}}static qualityRuntime(n,e=!0){return{options:{chart:{type:`scatter`,height:320,animation:!1},plotOptions:{series:{animation:!1}},title:{text:`Score versus runtime`},credits:{enabled:!1},exporting:{enabled:!0},accessibility:{enabled:!0},xAxis:{title:{text:`Runtime (ms)`}},yAxis:Fa.scoreAxis(n.objective,Fa.referenceValue(n.instance.references,n.objective),e),tooltip:{pointFormat:`Runtime: {point.x} ms<br/>Score: {point.y}<br/>Iteration: {point.custom.iteration}`},series:n.algorithms.map(i=>({type:`scatter`,name:i.name,data:n.results.filter(a=>a.algorithmName===i.name&&a.state===`SUCCEEDED`&&a.executionTime!==null&&Number.isFinite(a.objectives[n.objective])).map(a=>({x:a.executionTime/1e6,y:a.objectives[n.objective],custom:{iteration:a.iteration}}))}))}}}};var qc=class t{client=f(Pa);events=f(os);destroyRef=f(Ne);statusState=M(null);status=this.statusState.asReadonly();syncStateState=M(`connecting`);syncState=this.syncStateState.asReadonly();transportStateState=M(`connecting`);transportState=this.transportStateState.asReadonly();latestErrorState=M(null);latestError=this.latestErrorState.asReadonly();transportErrorState=M(null);transportError=this.transportErrorState.asReadonly();lastUpdatedState=M(null);lastUpdated=this.lastUpdatedState.asReadonly();selectedObjectiveState=M(null);selectedObjective=this.selectedObjectiveState.asReadonly();showReferenceState=M(!0);showReference=this.showReferenceState.asReadonly();selectedInstanceIdState=M(null);selectedInstanceId=this.selectedInstanceIdState.asReadonly();instancePageState=M(null);instancePage=this.instancePageState.asReadonly();resultPageState=M(null);resultPage=this.resultPageState.asReadonly();issuePageState=M(null);issuePage=this.issuePageState.asReadonly();artifactsState=M(null);artifacts=this.artifactsState.asReadonly();detailState=M(null);detail=this.detailState.asReadonly();inspectedResultState=M(null);inspectedResult=this.inspectedResultState.asReadonly();resultErrorState=M(null);resultError=this.resultErrorState.asReadonly();tabState=M(0);tab=this.tabState.asReadonly();instanceQueryState=M({experiment:``,search:``,offset:0,limit:25});instanceQuery=this.instanceQueryState.asReadonly();resultQueryState=M({instanceId:``,algorithm:``,state:``,search:``,offset:0,limit:25});resultQuery=this.resultQueryState.asReadonly();issueSeverityState=M(``);issueSeverity=this.issueSeverityState.asReadonly();issueOffsetState=M(0);issueOffset=this.issueOffsetState.asReadonly();seenIssueIdState=M(0);seenIssueId=this.seenIssueIdState.asReadonly();objectives=ne(()=>Object.entries(this.status()?.objectives??{}).map(([n,e])=>({name:n,mode:e})));terminal=ne(()=>[`SUCCEEDED`,`COMPLETED_WITH_ERRORS`,`FAILED`,`CANCELLED`].includes(this.status()?.phase??``));connectionLabel=ne(()=>this.syncState()===`retrying`?`Retrying · data may be stale`:this.syncState()===`connecting`?`Connecting`:this.transportState()===`live`?`Live`:`Live via HTTP`);unseenIssues=ne(()=>Math.max(0,(this.status()?.issueCount??0)-this.seenIssueId()));estimatedRemainingMillis=ne(()=>{let n=this.status();if(!n||n.phase!==`RUNNING`||n.elapsedMillis<1e4)return null;let e=n.counts.succeeded+n.counts.failed;return e<5||n.counts.cancelled>0?null:n.elapsedMillis/e*(n.counts.pending+n.counts.running)});charts=ne(()=>{let n=this.detail(),e=this.selectedObjective(),i=this.selectedObjectiveMode();return n&&e&&i&&n.objective===e?rs.instance(n,i,this.showReference()):null});selectedObjectiveMode=ne(()=>this.status()?.objectives[this.selectedObjective()??``]??null);distribution=ne(()=>this.detail()?rs.distribution(this.detail(),this.showReference()):null);qualityRuntime=ne(()=>this.detail()?rs.qualityRuntime(this.detail(),this.showReference()):null);lastEventId=ne(()=>(this.transportState(),this.lastUpdated(),this.events.lastEventId));progress=ne(()=>{let n=this.status(),e=this.detail(),i=e?.algorithms.find(o=>o.name===this.resultQuery().algorithm),a=i?.counts??e?.instance.counts??n?.counts;return{instances:{completed:n?.completedInstances??0,total:n?.instances??0},algorithms:{completed:e?.instance.completedAlgorithms??0,total:e?.instance.algorithms??0},repetitions:{completed:(a?.succeeded??0)+(a?.failed??0),total:a?.planned??0},repetitionScope:i?`selected algorithm`:e?`selected instance`:`execution`}});cache=new Map;timer=null;polling=!1;pending=!1;started=!1;destroyed=!1;generation=0;delay=500;forceDetail=!0;lastDetailFetch=0;resultRequest=0;constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.generation++,this.resultRequest++,this.clearTimer()})}start(){this.started||(this.started=!0,this.events.state$.pipe(En(this.destroyRef)).subscribe(n=>{this.transportStateState.set(n),n===`live`&&this.transportErrorState.set(null)}),this.events.errors$.pipe(En(this.destroyRef)).subscribe(n=>this.transportErrorState.set(this.message(n))),this.events.connected$.pipe(En(this.destroyRef)).subscribe(()=>{this.generation++,this.forceDetail=!0,this.refreshNow()}),this.events.envelopes$.pipe(En(this.destroyRef)).subscribe(n=>{n.type!==`PingEvent`&&(n.type===`ExecutionEndedEvent`||n.type===`InstanceProcessingEndedEvent`?(this.forceDetail=!0,this.refreshNow()):this.polling||this.schedule(Math.max(100,500-(Date.now()-(this.lastUpdated()?.getTime()??0)))))}),this.events.start(),this.refreshNow())}retryNow(){this.delay=500,this.events.retryNow(),this.refreshNow()}setReferenceVisible(n){this.showReferenceState.set(n)}selectObjective(n){!this.status()?.objectives[n]||this.selectedObjective()===n||(this.selectedObjectiveState.set(n),this.generation++,this.forceDetail=!0,this.detailState.set(null),this.refreshNow())}selectInstance(n){this.selectedInstanceIdState.set(n),this.generation++,this.forceDetail=!0;let e=this.cache.get(n);this.detailState.set(e?.objective===this.selectedObjective()?e:null),this.refreshNow()}selectTab(n){this.tabState.set(n),this.refreshNow()}filterInstances(n,e){this.instanceQueryState.set(s(r({},this.instanceQuery()),{experiment:n,search:e,offset:0})),this.invalidate()}pageInstances(n){this.instanceQueryState.update(e=>s(r({},e),{offset:n})),this.invalidate()}filterResults(n,e,i,a){this.resultQueryState.set(s(r({},this.resultQuery()),{instanceId:n,algorithm:e,state:i,search:a,offset:0})),this.invalidate()}pageResults(n){this.resultQueryState.update(e=>s(r({},e),{offset:n})),this.invalidate()}sortResults(n){this.resultQueryState.update(e=>s(r({},e),{order:n,offset:0})),this.invalidate()}filterIssues(n){this.issueSeverityState.set(n),this.issueOffsetState.set(0),this.invalidate()}pageIssues(n){this.issueOffsetState.set(n),this.invalidate()}markIssuesSeen(){this.seenIssueIdState.set(this.status()?.issueCount??0);try{localStorage.setItem(`mork.normal.issues.v1`,JSON.stringify({runId:this.status()?.runId,id:this.seenIssueId()}))}catch{}}async inspectResult(n){let e=this.status()?.runId;if(!e)return;let i=++this.resultRequest;this.inspectedResultState.set(null),this.resultErrorState.set(null);try{let[a,o]=await Promise.all([this.client.getResult(e,n),this.client.getArtifacts(e)]);!this.destroyed&&i===this.resultRequest&&e===this.status()?.runId&&a.runId===e&&(this.inspectedResultState.set(a),o.runId===e&&this.artifactsState.set(o))}catch(a){!this.destroyed&&i===this.resultRequest&&e===this.status()?.runId&&this.resultErrorState.set(this.message(a))}}closeResult(){this.resultRequest++,this.inspectedResultState.set(null),this.resultErrorState.set(null)}artifactUrl(n){return this.client.artifactUrl(this.status().runId,n)}exportUrl(n){return this.client.exportUrl(this.status().runId,this.resultQuery(),n)}refreshNow(){this.clearTimer(),this.polling?this.pending=!0:this.poll()}invalidate(){this.generation++,this.refreshNow()}async poll(){if(this.destroyed)return;this.polling=!0,this.pending=!1;let n=this.generation,e=2e3;try{let i=await this.client.getStatus();if(!this.current(n))return;let a=this.status();if(i.runId!==a?.runId){this.cache.clear(),this.detailState.set(null),this.inspectedResultState.set(null),this.resultRequest++,this.instancePageState.set(null),this.resultPageState.set(null),this.issuePageState.set(null),this.artifactsState.set(null),this.selectedInstanceIdState.set(null),this.selectedObjectiveState.set(Object.keys(i.objectives)[0]??null),this.instanceQueryState.set({experiment:``,search:``,offset:0,limit:25}),this.resultQueryState.set({instanceId:``,algorithm:``,state:``,search:``,offset:0,limit:25}),this.seenIssueIdState.set(0),this.forceDetail=!0;try{let o=JSON.parse(localStorage.getItem(`mork.normal.issues.v1`)??`null`);o?.runId===i.runId&&this.seenIssueIdState.set(o.id)}catch{}}if(a?.runId===i.runId&&i.revision<a.revision)throw new Error(`Monitoring revision moved backwards`);if(this.statusState.set(i),i.runId){let o=i.runId,r=await this.client.getInstances(o,this.instanceQuery());if(!this.current(n)||r.runId!==o)return;this.instancePageState.set(r),!this.selectedInstanceId()&&r.items.length&&this.selectedInstanceIdState.set(r.items.find(b=>b.counts.running>0)?.id??r.items[0].id);let s=this.selectedInstanceId(),c=this.selectedObjective(),m=s?this.cache.get(s):null,u=r.items.find(b=>b.id===s),g=!m||m.objective!==c||!u||m.revision!==u.revision,v=[`SUCCEEDED`,`COMPLETED_WITH_ERRORS`,`FAILED`,`CANCELLED`].includes(i.phase)&&i.revision!==a?.revision;if(s&&c&&g&&(this.forceDetail||v||Date.now()-this.lastDetailFetch>=2e3)){let b=await this.client.getInstance(o,s,c);if(!this.current(n)||b.runId!==o||b.objective!==this.selectedObjective())return;for(this.cache.delete(s),this.cache.set(s,b);this.cache.size>10;)this.cache.delete(this.cache.keys().next().value);this.detailState.set(b),this.lastDetailFetch=Date.now(),this.forceDetail=!1}else m&&m.objective===c&&this.detailState.set(m);if(this.tab()===1){let b=await this.client.getResults(o,this.resultQuery());if(!this.current(n)||b.runId!==o)return;this.resultPageState.set(b)}if(this.tab()===2||i.issueCount!==a?.issueCount||!this.issuePage()){let b=await this.client.getIssues(o,this.issueSeverity(),this.issueOffset());if(!this.current(n)||b.runId!==o)return;this.issuePageState.set(b)}if(this.tab()===3){let b=await this.client.getArtifacts(o);if(!this.current(n)||b.runId!==o)return;this.artifactsState.set(b)}}this.latestErrorState.set(null),this.lastUpdatedState.set(new Date),this.syncStateState.set(`live`),this.delay=500,e=this.terminal()||i.phase===`IDLE`?1e4:2e3}catch(i){if(!this.current(n))return;this.latestErrorState.set(this.message(i)),this.syncStateState.set(`retrying`),e=this.delay,this.delay=Math.min(3e4,this.delay*2)}finally{this.polling=!1,this.destroyed||this.schedule(this.pending||n!==this.generation?0:e)}}current(n){return!this.destroyed&&n===this.generation}message(n){return n instanceof Error?n.message:String(n)}schedule(n){this.destroyed||(this.clearTimer(),this.timer=setTimeout(()=>{this.poll()},n))}clearTimer(){this.timer&&clearTimeout(this.timer),this.timer=null}static ɵfac=function(e){return new(e||t)};static ɵprov=he({token:t,factory:t.ɵfac,providedIn:`root`})};var Ib=[{id:`evaluation-health`,label:`Evaluation health`},{id:`evaluation-cost`,label:`Evaluation cost`},{id:`elite-evolution`,label:`Elite rank evolution`},{id:`current-elites`,label:`Current elites`},{id:`run-information`,label:`Run information`}];var Mb=[{id:`normal-comparison`,label:`Algorithm comparison`},{id:`normal-scores`,label:`Scores`},{id:`normal-best`,label:`Best score across repetitions`},{id:`normal-distribution`,label:`Score distribution`},{id:`normal-runtime`,label:`Score versus runtime`}];var hm=`mork.autoconfig.panelPreferences.v1`;var La=class t{hiddenSignal=M(new Set);collapsedSignal=M(new Set);hidden=this.hiddenSignal.asReadonly();collapsed=this.collapsedSignal.asReadonly();constructor(){this.restore()}isHidden(n){return this.hiddenSignal().has(n)}isCollapsed(n){return this.collapsedSignal().has(n)}show(n){this.hiddenSignal.update(e=>this.without(e,n)),this.persist()}hide(n){this.hiddenSignal.update(e=>this.with(e,n)),this.persist()}toggleCollapsed(n){this.collapsedSignal.update(e=>e.has(n)?this.without(e,n):this.with(e,n)),this.persist()}reset(){this.hiddenSignal.set(new Set),this.collapsedSignal.set(new Set),localStorage.removeItem(hm)}restore(){try{let n=localStorage.getItem(hm);if(!n)return;let e=JSON.parse(n),i=new Set([...Ib,...Mb].map(a=>a.id));this.hiddenSignal.set(new Set((e.hidden??[]).filter(a=>i.has(a)))),this.collapsedSignal.set(new Set((e.collapsed??[]).filter(a=>i.has(a))))}catch{localStorage.removeItem(hm)}}with(n,e){let i=new Set(n);return i.add(e),i}without(n,e){let i=new Set(n);return i.delete(e),i}persist(){let n={hidden:[...this.hiddenSignal()],collapsed:[...this.collapsedSignal()]};localStorage.setItem(hm,JSON.stringify(n))}static ɵfac=function(e){return new(e||t)};static ɵprov=he({token:t,factory:t.ɵfac,providedIn:`root`})};var Ti=class t{model=jt.required();static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-dashboard-chart`]],inputs:{model:[1,`model`]},decls:1,vars:2,consts:[[1,`chart`,`highcharts-light`,3,`options`,`oneToOne`]],template:function(e,i){e&1&&z(0,`highcharts-chart`,0),e&2&&S(`options`,i.model().options)(`oneToOne`,!0)},dependencies:[fC],styles:[`[_nghost-%COMP%], .chart[_ngcontent-%COMP%]{display:block;width:100%;min-width:0}`]})};var uR=[`*`];function mR(t,n){t&1&&(l(0,`mat-card-content`),ue(1),d())}function pR(t,n){if(t&1){let e=ae();l(0,`mat-card`,1)(1,`mat-card-header`)(2,`mat-card-title`),p(3),d(),z(4,`span`,2),l(5,`button`,3),C(`click`,function(){A(e);let a=_();return O(a.preferences.toggleCollapsed(a.panelId()))}),p(6),d(),l(7,`button`,4),p(8,` ⋮ `),d(),l(9,`mat-menu`,null,0)(11,`button`,5),C(`click`,function(){A(e);let a=_();return O(a.preferences.toggleCollapsed(a.panelId()))}),p(12),d(),l(13,`button`,5),C(`click`,function(){A(e);let a=_();return O(a.preferences.hide(a.panelId()))}),p(14,` Hide panel `),d()()(),N(15,mR,2,0,`mat-card-content`),d()}if(t&2){let e=mt(10),i=_();h(3),w(i.title()),h(2),ee(`aria-label`,i.preferences.isCollapsed(i.panelId())?`Expand `+i.title():`Collapse `+i.title()),h(),H(` `,i.preferences.isCollapsed(i.panelId())?`+`:`−`,` `),h(),S(`matMenuTriggerFor`,e),ee(`aria-label`,i.title()+` options`),h(5),H(` `,i.preferences.isCollapsed(i.panelId())?`Expand panel`:`Collapse panel`,` `),h(3),T(i.preferences.isCollapsed(i.panelId())?-1:15)}}var ss=class t{preferences=f(La);panelId=jt.required();title=jt.required();static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-monitor-panel`]],inputs:{panelId:[1,`panelId`],title:[1,`title`]},ngContentSelectors:uR,decls:1,vars:1,consts:[[`panelMenu`,`matMenu`],[`appearance`,`outlined`],[1,`spacer`],[`mat-button`,``,`type`,`button`,1,`collapse`,3,`click`],[`mat-button`,``,`type`,`button`,1,`menu`,3,`matMenuTriggerFor`],[`mat-menu-item`,``,`type`,`button`,3,`click`]],template:function(e,i){e&1&&(Oe(),N(0,pR,16,7,`mat-card`,1)),e&2&&T(i.preferences.isHidden(i.panelId())?-1:0)},dependencies:[Tt,Zt,jn,Vn,Bn,Ia,ka,ns,Ni,sa,ts],styles:[`[_nghost-%COMP%]{display:block;min-width:0}mat-card[_ngcontent-%COMP%]{height:100%}mat-card-header[_ngcontent-%COMP%]{min-height:3.5rem;align-items:center;padding-bottom:.5rem}mat-card-title[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-title-medium);font-weight:650}mat-card-content[_ngcontent-%COMP%]{min-width:0}.spacer[_ngcontent-%COMP%]{flex:1}.collapse[_ngcontent-%COMP%], .menu[_ngcontent-%COMP%]{min-width:2.25rem;padding:0;font-size:1.25rem}`]})};var hR=(t,n)=>n.id;function fR(t,n){if(t&1&&(l(0,`p`,0),p(1),d()),t&2){let e=n.$implicit;h(),qi(` `,e.severity,` · `,e.scope,`: `,e.exceptionType,` — `,e.message,` `)}}function gR(t,n){t&1&&Sa(0)}function bR(t,n){if(t&1&&at(0,gR,1,0,`ng-container`,2),t&2){let e=_();S(`ngComponentOutlet`,n)(`ngComponentOutletInputs`,e.rendererInputs())}}function vR(t,n){if(t&1&&(l(0,`details`,1)(1,`summary`),p(2,`Solution view data`),d(),l(3,`pre`),p(4),Q(5,`json`),d()()),t&2){let e=_();h(4),w(Ce(5,1,e.detail().rendering.data))}}function yR(t,n){if(t&1&&(l(0,`p`,0),p(1),d()),t&2){let e=_();h(),H(` `,e.detail().rendering.state===`UNSUPPORTED`?`A problem-specific solution view is not provided for this project.`:`This solution view is no longer retained.`,` Metadata remains available; exported solution files are listed below or in Artifacts. `)}}var _R=new D(`Mork solution renderers`,{providedIn:`root`,factory:()=>({})});var fm=class t{detail=jt.required();selectedObjective=jt.required();registered=f(_R);renderer=ne(()=>this.detail().rendering.state===`AVAILABLE`?this.registered[this.detail().rendering.rendererId??``]??null:null);rendererInputs=ne(()=>({result:this.detail().result,selectedObjective:this.selectedObjective(),view:this.detail().rendering.data}));static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-solution-renderer`]],inputs:{detail:[1,`detail`],selectedObjective:[1,`selectedObjective`]},decls:58,vars:27,consts:[[1,`note`],[`open`,``],[4,`ngComponentOutlet`,`ngComponentOutletInputs`]],template:function(e,i){if(e&1&&(l(0,`dl`)(1,`div`)(2,`dt`),p(3,`Instance / experiment`),d(),l(4,`dd`),p(5),d()(),l(6,`div`)(7,`dt`),p(8,`Algorithm`),d(),l(9,`dd`),p(10),d()(),l(11,`div`)(12,`dt`),p(13,`Iteration / seed`),d(),l(14,`dd`),p(15),d()(),l(16,`div`)(17,`dt`),p(18,`Outcome`),d(),l(19,`dd`),p(20),d()(),l(21,`div`)(22,`dt`),p(23),d(),l(24,`dd`),p(25),Q(26,`number`),d()(),l(27,`div`)(28,`dt`),p(29,`Runtime`),d(),l(30,`dd`),p(31),Q(32,`number`),d()(),l(33,`div`)(34,`dt`),p(35,`Time to best`),d(),l(36,`dd`),p(37),Q(38,`number`),d()(),l(39,`div`)(40,`dt`),p(41,`Result ID`),d(),l(42,`dd`),p(43),d()()(),l(44,`details`)(45,`summary`),p(46,`All objective values and custom properties`),d(),l(47,`pre`),p(48),Q(49,`json`),d(),l(50,`pre`),p(51),Q(52,`json`),d()(),ve(53,fR,2,4,`p`,0,hR),N(55,bR,1,2,`ng-container`)(56,vR,6,3,`details`,1)(57,yR,2,1,`p`,0)),e&2){let a,o=i.detail().result;h(5),Ue(``,o.instanceName,` / `,o.experimentName),h(5),w(o.algorithmName),h(5),Ue(``,o.iteration,` / `,o.seed),h(5),w(o.state),h(3),w(i.selectedObjective()??`Objective`),h(2),H(` `,He(26,14,o.objectives[i.selectedObjective()??``],`1.0-4`)??`Unavailable`,` `),h(6),H(` `,o.executionTime===null?`Unavailable`:He(32,17,o.executionTime/1e6,`1.0-2`)+` ms`,` `),h(6),H(` `,o.timeToBest===null?`Unavailable`:He(38,20,o.timeToBest/1e6,`1.0-2`)+` ms`,` `),h(6),w(o.resultId),h(5),w(Ce(49,23,o.objectives)),h(3),w(Ce(52,25,o.properties)),h(2),ye(i.detail().issues),h(2),T((a=i.renderer())?55:i.detail().rendering.state===`AVAILABLE`?56:57,a)}},dependencies:[cg,xo,Sn],styles:[`dl[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:.75rem}dl[_ngcontent-%COMP%] > div[_ngcontent-%COMP%]{display:grid;gap:.25rem}dt[_ngcontent-%COMP%], .note[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant)}dd[_ngcontent-%COMP%]{margin:0;overflow-wrap:anywhere}pre[_ngcontent-%COMP%]{overflow:auto;max-height:24rem;white-space:pre-wrap}@media(max-width:42rem){dl[_ngcontent-%COMP%]{grid-template-columns:1fr}}`]})};var cs=()=>[];var Yc=(t,n)=>n.id;var Nb=(t,n)=>n.name;var xR=(t,n)=>n.resultId;function CR(t,n){if(t&1){let e=ae();l(0,`button`,8),C(`click`,function(){let a=A(e).$implicit,o=_();return O(o.preferences.isHidden(a.id)?o.preferences.show(a.id):o.preferences.hide(a.id))}),p(1),d()}if(t&2){let e=n.$implicit,i=_();h(),Ue(` `,i.preferences.isHidden(e.id)?`Show`:`Hide`,` `,e.label,` `)}}function SR(t,n){if(t&1){let e=ae();l(0,`mat-card`,9)(1,`mat-card-content`)(2,`strong`),p(3,`Connection problem · showing last known data`),d(),l(4,`p`),p(5),d(),l(6,`button`,34),C(`click`,function(){A(e);return O(_().store.retryNow())}),p(7,`Retry now`),d()()()}t&2&&(h(5),w(n))}function wR(t,n){if(t&1&&(l(0,`mat-option`,16),p(1),d()),t&2){let e=n.$implicit;S(`value`,e.name),h(),Ue(``,e.name,` · `,e.mode===`MINIMIZE`?`Minimize`:`Maximize`)}}function ER(t,n){t&1&&(p(0),Q(1,`date`)),t&2&&H(` Last refreshed `,He(1,1,n,`mediumTime`),`. `)}function DR(t,n){if(t&1&&(l(0,`p`,24),p(1),d()),t&2){let e=_();h(),H(` Approximate time remaining: `,e.elapsedLabel(n),` · based on observed throughput; different instance sizes can change this estimate. `)}}function kR(t,n){if(t&1){let e=ae();l(0,`button`,35),C(`click`,function(){A(e);return O(_().store.selectTab(2))}),p(1),d()}if(t&2){let e=_();h(),H(` `,e.store.unseenIssues(),` unseen issues `)}}function IR(t,n){if(t&1&&(l(0,`mat-option`,16),p(1),d()),t&2){let e=n.$implicit;S(`value`,e.name),h(),w(e.name)}}function MR(t,n){if(t&1){let e=ae();l(0,`tr`)(1,`td`)(2,`button`,35),C(`click`,function(){let a=A(e).$implicit;return O(_(2).store.selectInstance(a.id))}),p(3),d()(),l(4,`td`),p(5),d(),l(6,`td`),p(7),d(),l(8,`td`),p(9),d(),l(10,`td`),p(11),d()()}if(t&2){let e=n.$implicit,i=_(2);W(`selected`,e.id===i.store.selectedInstanceId()),h(3),H(` `,e.instanceName,` `),h(2),w(e.experimentName),h(2),Ue(` `,e.counts.succeeded+e.counts.failed,` / `,e.counts.planned,` `),h(2),w(e.counts.running),h(2),Ue(` `,e.counts.failed,` failed · `,e.finalized?`finalized`:`processing`,` `)}}function NR(t,n){t&1&&(l(0,`tr`)(1,`td`,46),p(2,`No matching instances yet.`),d()())}function TR(t,n){if(t&1&&(l(0,`p`,24),p(1),d()),t&2){let e=_();h(),Ue(` Charts show `,e.results.length,` sampled points from `,e.resultCount,` repetitions. Statistics are exact; full data is available in Results and exports. `)}}function AR(t,n){if(t&1&&(p(0),Q(1,`number`)),t&2){let e=_().$implicit;H(` (`,He(1,1,e.objective.percentageGap,`1.0-2`),`%) `)}}function OR(t,n){if(t&1){let e=ae();l(0,`tr`)(1,`td`),p(2),d(),l(3,`td`),p(4),d(),l(5,`td`),p(6),d(),l(7,`td`),p(8),Q(9,`number`),d(),l(10,`td`),p(11),Q(12,`number`),d(),l(13,`td`),p(14),Q(15,`number`),Q(16,`number`),d(),l(17,`td`),p(18),Q(19,`number`),N(20,AR,2,4),d(),l(21,`td`),p(22),Q(23,`number`),d(),l(24,`td`),p(25),Q(26,`number`),d(),l(27,`td`)(28,`button`,49),C(`click`,function(){let a=A(e).$implicit;return O(_(3).store.inspectResult(a.bestResultId))}),p(29,` Best result `),d()()()}if(t&2){let e=n.$implicit;h(2),w(e.name),h(2),Ue(` `,e.counts.succeeded,` / `,e.counts.succeeded+e.counts.failed,` `),h(2),w(e.objective.count),h(2),w(He(9,13,e.objective.best,`1.0-4`)??`—`),h(3),w(He(12,16,e.objective.median,`1.0-4`)??`—`),h(3),Ue(` `,He(15,19,e.objective.mean,`1.0-4`)??`—`,` ± `,He(16,22,e.objective.standardDeviation,`1.0-4`)??`—`,` `),h(4),H(` `,He(19,25,e.objective.absoluteGap,`1.0-4`)??`—`,` `),h(2),T(e.objective.percentageGap!==null?20:-1),h(2),H(``,He(23,28,e.runtimeMillis.median,`1.0-2`)??`—`,` ms`),h(3),H(``,He(26,31,e.timeToBestMillis.median,`1.0-2`)??`—`,` ms`),h(3),S(`disabled`,!e.bestResultId)}}function RR(t,n){if(t&1){let e=ae();l(0,`mat-checkbox`,56),C(`change`,function(a){A(e);return O(_(4).store.setReferenceVisible(a.checked))}),p(1),d(),l(2,`p`,24),p(3),d()}if(t&2){let e=_(),i=_(),a=_(2);S(`checked`,a.store.showReference()),h(),Ue(` Show reference (`,i.objective,`: `,e.referenceValue,`) `),h(2),H(` The red dashed line marks the reference in all score charts. `,a.store.status()?.objectives?.[i.objective]===`MAXIMIZE`?`Higher`:`Lower`,` scores are better. `)}}function PR(t,n){if(t&1&&(l(0,`p`,24),p(1),d()),t&2){let e=_(2);h(),H(` No reference value is available for `,e.objective,`. `)}}function FR(t,n){if(t&1&&z(0,`app-dashboard-chart`,52),t&2)S(`model`,_().scoreChart)}function LR(t,n){if(t&1&&z(0,`app-dashboard-chart`,52),t&2)S(`model`,_().convergenceChart)}function VR(t,n){t&1&&z(0,`app-dashboard-chart`,52),t&2&&S(`model`,n)}function BR(t,n){t&1&&z(0,`app-dashboard-chart`,52),t&2&&S(`model`,n)}function jR(t,n){if(t&1&&(N(0,RR,4,4)(1,PR,2,1,`p`,24),l(2,`div`,50)(3,`app-monitor-panel`,51),N(4,FR,1,1,`app-dashboard-chart`,52),d(),l(5,`app-monitor-panel`,53),N(6,LR,1,1,`app-dashboard-chart`,52),l(7,`p`,24),p(8,` Independent repetitions, not the optimization trajectory within a single run. Failures are gaps. `),d()(),l(9,`app-monitor-panel`,54),N(10,VR,1,1,`app-dashboard-chart`,52),d(),l(11,`app-monitor-panel`,55),N(12,BR,1,1,`app-dashboard-chart`,52),d()()),t&2){let e,i,a=_(3);T(n.referenceValue!==null?0:1),h(4),T(!a.preferences.isHidden(`normal-scores`)&&!a.preferences.isCollapsed(`normal-scores`)?4:-1),h(2),T(!a.preferences.isHidden(`normal-best`)&&!a.preferences.isCollapsed(`normal-best`)?6:-1),h(4),T((e=!a.preferences.isHidden(`normal-distribution`)&&!a.preferences.isCollapsed(`normal-distribution`)&&a.store.distribution())?10:-1,e),h(2),T((i=!a.preferences.isHidden(`normal-runtime`)&&!a.preferences.isCollapsed(`normal-runtime`)&&a.store.qualityRuntime())?12:-1,i)}}function zR(t,n){if(t&1&&(l(0,`h2`,47),p(1),l(2,`small`),p(3),d()(),N(4,TR,2,2,`p`,24),l(5,`app-monitor-panel`,48)(6,`div`,41)(7,`table`)(8,`caption`,42),p(9),d(),l(10,`thead`)(11,`tr`)(12,`th`),p(13,`Algorithm`),d(),l(14,`th`),p(15,`Successful / attempted`),d(),l(16,`th`),p(17,`Valid scores`),d(),l(18,`th`),p(19,`Best`),d(),l(20,`th`),p(21,`Median`),d(),l(22,`th`),p(23,`Mean ± SD`),d(),l(24,`th`),p(25,`Gap to reference`),d(),l(26,`th`),p(27,`Runtime median`),d(),l(28,`th`),p(29,`Time-to-best median`),d(),l(30,`th`),p(31,`Details`),d()()(),l(32,`tbody`),ve(33,OR,30,34,`tr`,null,Nb),d()()(),l(35,`p`,24),p(36,` Statistics use successful results with finite values. Scores from different instances are not averaged. Negative gaps improve on the reference. `),d()(),N(37,jR,13,5)),t&2){let e,i=n,a=_(2);h(),H(` `,i.instance.instanceName,` `),h(2),w(i.instance.experimentName),h(),T(i.sampled?4:-1),h(5),H(` `,i.objective,` statistics by algorithm `),h(24),ye(i.algorithms),h(4),T((e=a.store.charts())?37:-1,e)}}function HR(t,n){t&1&&(l(0,`p`,45),p(1,`Select an instance to inspect its results and charts.`),d())}function UR(t,n){if(t&1){let e=ae();l(0,`mat-card`,36)(1,`mat-card-content`)(2,`h2`),p(3,`Instances`),d(),l(4,`div`,37)(5,`mat-form-field`,38)(6,`mat-label`),p(7,`Experiment`),d(),l(8,`mat-select`,15),C(`selectionChange`,function(a){A(e);let o=_();return O(o.store.filterInstances(a.value,o.store.instanceQuery().search))}),l(9,`mat-option`,39),p(10,`All experiments`),d(),ve(11,IR,2,2,`mat-option`,16,Nb),d()(),l(13,`mat-form-field`,38)(14,`mat-label`),p(15,`Search instances`),d(),l(16,`input`,40),C(`input`,function(a){A(e);let o=_();return O(o.store.filterInstances(o.store.instanceQuery().experiment,a.target.value))}),d()()(),l(17,`div`,41)(18,`table`)(19,`caption`,42),p(20,` Instances across the execution `),d(),l(21,`thead`)(22,`tr`)(23,`th`),p(24,`Instance`),d(),l(25,`th`),p(26,`Experiment`),d(),l(27,`th`),p(28,`Completed repetitions`),d(),l(29,`th`),p(30,`Running`),d(),l(31,`th`),p(32,`Outcome`),d()()(),l(33,`tbody`),ve(34,MR,12,9,`tr`,43,Yc,!1,NR,3,0,`tr`),d()()(),l(37,`mat-paginator`,44),C(`page`,function(a){A(e);return O(_().store.pageInstances(a.pageIndex*a.pageSize))}),d()()(),N(38,zR,38,5)(39,HR,2,0,`p`,45)}if(t&2){let e,i=_();h(8),S(`value`,i.store.instanceQuery().experiment),h(3),ye(i.store.status()?.experiments??Bt(7,cs)),h(5),S(`value`,i.store.instanceQuery().search),h(18),ye(i.store.instancePage()?.items??Bt(8,cs)),h(3),S(`length`,i.store.instancePage()?.total??0)(`pageSize`,25)(`pageIndex`,i.store.instanceQuery().offset/25),h(),T((e=i.store.detail())?38:39,e)}}function $R(t,n){if(t&1&&(l(0,`mat-option`,16),p(1),d()),t&2){let e=n.$implicit,i=_(2);S(`value`,e),h(),w(e?i.phaseLabel(e):`All outcomes`)}}function GR(t,n){if(t&1&&(l(0,`a`,63),p(1,`CSV`),d(),l(2,`a`,63),p(3,`JSON`),d()),t&2){let e=_(2);S(`href`,e.store.exportUrl(`csv`),Cn),h(2),S(`href`,e.store.exportUrl(`json`),Cn)}}function WR(t,n){if(t&1){let e=ae();l(0,`tr`)(1,`td`),p(2),d(),l(3,`td`),p(4),d(),l(5,`td`),p(6),d(),l(7,`td`),p(8),d(),l(9,`td`),p(10),d(),l(11,`td`),p(12),Q(13,`number`),d(),l(14,`td`),p(15),Q(16,`number`),d(),l(17,`td`)(18,`button`,35),C(`click`,function(){let a=A(e).$implicit;return O(_(2).store.inspectResult(a.resultId))}),p(19,` Inspect `),d()()()}if(t&2){let e=n.$implicit,i=_(2);h(2),Ue(``,e.experimentName,` / `,e.instanceName),h(2),w(e.algorithmName),h(2),w(e.iteration),h(2),w(e.seed),h(2),w(i.phaseLabel(e.state)),h(2),H(` `,He(13,8,e.objectives[i.store.selectedObjective()??``],`1.0-4`)??`—`,` `),h(3),H(` `,e.executionTime===null?`—`:He(16,11,e.executionTime/1e6,`1.0-2`)+` ms`,` `)}}function qR(t,n){t&1&&(l(0,`tr`)(1,`td`,64),p(2,`No matching repetitions.`),d()())}function YR(t,n){if(t&1){let e=ae();l(0,`mat-card`,36)(1,`mat-card-content`)(2,`div`,37)(3,`mat-form-field`,38)(4,`mat-label`),p(5,`Search experiment, instance or algorithm`),d(),l(6,`input`,40),C(`input`,function(a){A(e);let o=_();return O(o.store.filterResults(o.store.resultQuery().instanceId,o.store.resultQuery().algorithm,o.store.resultQuery().state,a.target.value))}),d()(),l(7,`mat-form-field`,38)(8,`mat-label`),p(9,`Outcome`),d(),l(10,`mat-select`,15),C(`selectionChange`,function(a){A(e);let o=_();return O(o.store.filterResults(o.store.resultQuery().instanceId,o.store.resultQuery().algorithm,a.value,o.store.resultQuery().search))}),ve(11,$R,2,2,`mat-option`,16,ii),d()(),l(13,`mat-form-field`,38)(14,`mat-label`),p(15,`Algorithm name`),d(),l(16,`input`,40),C(`input`,function(a){A(e);let o=_();return O(o.store.filterResults(o.store.resultQuery().instanceId,a.target.value,o.store.resultQuery().state,o.store.resultQuery().search))}),d()(),l(17,`mat-form-field`,38)(18,`mat-label`),p(19,`Sort results`),d(),l(20,`mat-select`,15),C(`selectionChange`,function(a){A(e);return O(_().store.sortResults(a.value))}),l(21,`mat-option`,57),p(22,`Planned order`),d(),l(23,`mat-option`,58),p(24,`Instance name`),d(),l(25,`mat-option`,59),p(26,`Algorithm name`),d(),l(27,`mat-option`,60),p(28,`Outcome`),d(),l(29,`mat-option`,61),p(30,`Runtime · fastest first`),d()()(),l(31,`button`,35),C(`click`,function(){A(e);let a=_();return O(a.store.filterResults(a.store.selectedInstanceId()??``,a.store.resultQuery().algorithm,a.store.resultQuery().state,a.store.resultQuery().search))}),p(32,` Selected instance only `),d(),l(33,`button`,35),C(`click`,function(){A(e);return O(_().store.filterResults(``,``,``,``))}),p(34,` Clear filters `),d(),N(35,GR,4,2),d(),l(36,`div`,41)(37,`table`)(38,`caption`,42),p(39,` Repetition result ledger `),d(),l(40,`thead`)(41,`tr`)(42,`th`),p(43,`Experiment / instance`),d(),l(44,`th`),p(45,`Algorithm`),d(),l(46,`th`),p(47,`Iteration`),d(),l(48,`th`),p(49,`Seed`),d(),l(50,`th`),p(51,`Outcome`),d(),l(52,`th`),p(53),d(),l(54,`th`),p(55,`Runtime`),d(),l(56,`th`),p(57,`Details`),d()()(),l(58,`tbody`),ve(59,WR,20,14,`tr`,null,xR,!1,qR,3,0,`tr`),d()()(),l(62,`mat-paginator`,62),C(`page`,function(a){A(e);return O(_().store.pageResults(a.pageIndex*a.pageSize))}),d()()()}if(t&2){let e=_();h(6),S(`value`,e.store.resultQuery().search),h(4),S(`value`,e.store.resultQuery().state),h(),ye(e.workStates),h(5),S(`value`,e.store.resultQuery().algorithm),h(4),S(`value`,e.store.resultQuery().order??`PLANNED`),h(15),T(e.store.status()?.runId?35:-1),h(18),w(e.store.selectedObjective()??`Objective`),h(6),ye(e.store.resultPage()?.items??Bt(10,cs)),h(3),S(`length`,e.store.resultPage()?.total??0)(`pageSize`,25)(`pageIndex`,e.store.resultQuery().offset/25)}}function KR(t,n){if(t&1&&p(0),t&2){let e=_().$implicit;Ue(` · iteration `,e.iteration,` · seed `,e.seed,` `)}}function QR(t,n){if(t&1){let e=ae();l(0,`button`,35),C(`click`,function(){A(e);let a=_().$implicit;return O(_(2).store.inspectResult(a.resultId))}),p(1,` Inspect result `),d()}}function XR(t,n){if(t&1&&(l(0,`mat-card`,68)(1,`mat-card-content`)(2,`strong`),p(3),d(),l(4,`p`),p(5),d(),l(6,`p`),p(7),N(8,KR,1,2),p(9),Q(10,`date`),d(),N(11,QR,2,0,`button`,25),d()()),t&2){let e=n.$implicit;h(3),Rn(``,e.severity,` · `,e.scope,` · `,e.exceptionType),h(2),w(e.message??`No details provided`),h(2),Rn(` `,e.experimentName,` / `,e.instanceName,` / `,e.algorithmName,` `),h(),T(e.iteration!==null?8:-1),h(),H(` · `,He(10,10,e.timestamp,`medium`),` `),h(2),T(e.resultId?11:-1)}}function ZR(t,n){t&1&&(l(0,`p`,45),p(1,`No experiment issues have been reported.`),d())}function JR(t,n){if(t&1){let e=ae();l(0,`div`,37)(1,`mat-form-field`,38)(2,`mat-label`),p(3,`Severity`),d(),l(4,`mat-select`,15),C(`selectionChange`,function(a){A(e);return O(_().store.filterIssues(a.value))}),l(5,`mat-option`,39),p(6,`All severities`),d(),l(7,`mat-option`,65),p(8,`Fatal`),d(),l(9,`mat-option`,66),p(10,`Error`),d(),l(11,`mat-option`,67),p(12,`Warning`),d()()(),l(13,`button`,35),C(`click`,function(){A(e);return O(_().store.markIssuesSeen())}),p(14,`Mark issues seen`),d()(),ve(15,XR,12,13,`mat-card`,68,Yc,!1,ZR,2,0,`p`,45),l(18,`mat-paginator`,69),C(`page`,function(a){A(e);return O(_().store.pageIssues(a.pageIndex*a.pageSize))}),d()}if(t&2){let e=_();h(4),S(`value`,e.store.issueSeverity()),h(11),ye(e.store.issuePage()?.items??Bt(5,cs)),h(3),S(`length`,e.store.issuePage()?.total??0)(`pageSize`,25)(`pageIndex`,e.store.issueOffset()/25)}}function eP(t,n){if(t&1&&(l(0,`a`,63),p(1,`Download results CSV`),d(),l(2,`a`,63),p(3,`Download results JSON`),d()),t&2){let e=_(2);S(`href`,e.store.exportUrl(`csv`),Cn),h(2),S(`href`,e.store.exportUrl(`json`),Cn)}}function tP(t,n){if(t&1&&(l(0,`tr`)(1,`td`)(2,`a`,70),p(3),d()(),l(4,`td`),p(5),d(),l(6,`td`),p(7),Q(8,`number`),d(),l(9,`td`),p(10),Q(11,`date`),d()()),t&2){let e=n.$implicit,i=_(2);h(2),S(`href`,i.store.artifactUrl(e.id),Cn),h(),w(e.filename),h(2),w(e.experimentName),h(2),w(Ce(8,5,e.size)),h(3),w(He(11,7,e.modifiedAt,`medium`))}}function nP(t,n){t&1&&(l(0,`tr`)(1,`td`,71),p(2,`No exported artifacts are available yet.`),d()())}function iP(t,n){if(t&1&&(l(0,`p`),p(1,` Only successfully exported, registered files are available. Result exports use the current Results filters. `),d(),N(2,eP,4,2),l(3,`div`,41)(4,`table`)(5,`thead`)(6,`tr`)(7,`th`),p(8,`File`),d(),l(9,`th`),p(10,`Experiment`),d(),l(11,`th`),p(12,`Bytes`),d(),l(13,`th`),p(14,`Updated`),d()()(),l(15,`tbody`),ve(16,tP,12,10,`tr`,null,Yc,!1,nP,3,0,`tr`),d()()()),t&2){let e=_();h(2),T(e.store.status()?.runId?2:-1),h(14),ye(e.store.artifacts()?.artifacts??Bt(2,cs))}}function aP(t,n){t&1&&(l(0,`p`),p(1),d()),t&2&&(h(),w(n))}function oP(t,n){if(t&1){let e=ae();l(0,`mat-card`,72)(1,`mat-card-content`)(2,`h2`),p(3,`Transport diagnostics`),d(),l(4,`p`),p(5),d(),l(6,`p`),p(7),d(),l(8,`p`),p(9),d(),l(10,`p`),p(11),d(),N(12,aP,2,1,`p`),l(13,`p`),p(14,` Summaries and results come from backend snapshots. Connection health is independent of execution outcome. `),d(),l(15,`button`,35),C(`click`,function(){A(e);return O(_().store.retryNow())}),p(16,`Reconnect / refresh`),d()()()}if(t&2){let e,i=_();h(5),H(`Run: `,i.store.status()?.runId??`No execution`),h(2),H(`Revision: `,i.store.status()?.revision??0),h(2),H(`WebSocket: `,i.store.transportState()),h(2),H(`Last processed notification ID: `,i.store.lastEventId()),h(),T((e=i.store.transportError())?12:-1,e)}}function rP(t,n){if(t&1){let e=ae();l(0,`mat-card`,9)(1,`mat-card-content`),p(2),l(3,`button`,35),C(`click`,function(){A(e);return O(_().store.closeResult())}),p(4,`Close`),d()()()}t&2&&(h(2),w(n))}function sP(t,n){if(t&1&&(l(0,`a`,63),p(1),d()),t&2){let e=_().$implicit;S(`href`,_(2).store.artifactUrl(e.id),Cn),h(),w(e.filename)}}function cP(t,n){if(t&1&&N(0,sP,2,2,`a`,63),t&2){let e=n.$implicit,i=_();T(e.resultId===i.result.resultId?0:-1)}}function lP(t,n){if(t&1){let e=ae();l(0,`mat-card`,33)(1,`mat-card-content`)(2,`div`,73)(3,`h2`),p(4,`Result details`),d(),l(5,`button`,35),C(`click`,function(){A(e);return O(_().store.closeResult())}),p(6,`Close`),d()(),z(7,`app-solution-renderer`,74),ve(8,cP,1,1,null,null,Yc),d()()}if(t&2){let e=_();h(7),S(`detail`,n)(`selectedObjective`,e.store.selectedObjective()),h(),ye(e.store.artifacts()?.artifacts??Bt(2,cs))}}var gm=class t{store=f(qc);preferences=f(La);panels=Mb;workStates=[``,`PENDING`,`RUNNING`,`SUCCEEDED`,`FAILED`,`CANCELLED`];ngOnInit(){this.store.start()}progressValue(n,e){return e>0?Math.min(100,n/e*100):0}elapsedLabel(n){let e=Math.floor(n/1e3);return e>=3600?Math.floor(e/3600)+`h `+Math.floor(e%3600/60)+`m`:Math.floor(e/60)+`m `+e%60+`s`}phaseLabel(n){return n.toLowerCase().replaceAll(`_`,` `)}static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-standard-dashboard`]],features:[$e([qc,os,Pa])],decls:113,vars:31,consts:[[`panelMenu`,`matMenu`],[1,`topbar`],[`src`,`favicon.svg`,`alt`,`Mork`,1,`logo`],[1,`brand`],[1,`toolbar-spacer`],[`aria-label`,`Connection state`],[`mat-button`,``,`aria-label`,`Choose dashboard panels`,3,`matMenuTriggerFor`],[`mat-menu-item`,``],[`mat-menu-item`,``,3,`click`],[`role`,`alert`,1,`error-card`],[`appearance`,`outlined`,1,`status-card`],[`aria-label`,`Execution summary`,1,`summary`],[1,`eyebrow`],[1,`phase`],[`appearance`,`outline`,`subscriptSizing`,`dynamic`],[3,`selectionChange`,`value`],[3,`value`],[`aria-label`,`Repetition outcomes`,1,`metrics`],[`aria-label`,`Execution progress`,1,`progress-grid`],[1,`progress-item`],[1,`progress-label`],[`mode`,`determinate`,`aria-label`,`Finalized instances in execution`,3,`value`],[`mode`,`determinate`,`aria-label`,`Finalized algorithms in selected instance`,3,`value`],[`mode`,`determinate`,`aria-label`,`Completed repetitions in selection`,3,`value`],[1,`status-footnote`],[`mat-button`,``],[`animationDuration`,`0ms`,3,`selectedIndexChange`,`selectedIndex`],[`label`,`Overview`],[`matTabContent`,``],[`label`,`Results`],[`label`,`Issues`],[`label`,`Artifacts`],[`label`,`Diagnostics`],[`appearance`,`outlined`,`aria-label`,`Result details`,1,`result-detail`],[`mat-flat-button`,``,3,`click`],[`mat-button`,``,3,`click`],[`appearance`,`outlined`,1,`browser`],[1,`filters`],[`appearance`,`outline`],[`value`,``],[`matInput`,``,3,`input`,`value`],[1,`table-scroll`],[1,`sr-only`],[3,`selected`],[`aria-label`,`Instance pages`,3,`page`,`length`,`pageSize`,`pageIndex`],[1,`empty-state`],[`colspan`,`5`],[1,`detail-title`],[`panelId`,`normal-comparison`,`title`,`Algorithm comparison`],[`mat-button`,``,3,`click`,`disabled`],[1,`chart-grid`],[`panelId`,`normal-scores`,`title`,`Scores`],[3,`model`],[`panelId`,`normal-best`,`title`,`Best score across repetitions`],[`panelId`,`normal-distribution`,`title`,`Score distribution`],[`panelId`,`normal-runtime`,`title`,`Score versus runtime`],[1,`reference-toggle`,3,`change`,`checked`],[`value`,`PLANNED`],[`value`,`INSTANCE`],[`value`,`ALGORITHM`],[`value`,`STATE`],[`value`,`RUNTIME`],[`aria-label`,`Result pages`,3,`page`,`length`,`pageSize`,`pageIndex`],[`mat-button`,``,3,`href`],[`colspan`,`8`],[`value`,`FATAL`],[`value`,`ERROR`],[`value`,`WARNING`],[`appearance`,`outlined`,1,`issue`],[`aria-label`,`Issue pages`,3,`page`,`length`,`pageSize`,`pageIndex`],[3,`href`],[`colspan`,`4`],[1,`browser`],[1,`result-heading`],[3,`detail`,`selectedObjective`]],template:function(e,i){if(e&1&&(l(0,`mat-toolbar`,1),z(1,`img`,2),l(2,`span`,3),p(3,`Experiment monitor`),d(),z(4,`span`,4),l(5,`mat-chip-set`,5)(6,`mat-chip`),p(7),d()(),l(8,`button`,6),p(9,` Panels `),d(),l(10,`mat-menu`,null,0),ve(12,CR,2,2,`button`,7,Yc),l(14,`button`,8),C(`click`,function(){return i.preferences.reset()}),p(15,`Reset panels`),d()()(),l(16,`main`),N(17,SR,8,1,`mat-card`,9),l(18,`mat-card`,10)(19,`mat-card-content`)(20,`section`,11)(21,`div`)(22,`span`,12),p(23,`Execution`),d(),l(24,`strong`,13),p(25),d()(),l(26,`div`)(27,`span`,12),p(28,`Experiment`),d(),l(29,`strong`),p(30),d()(),l(31,`div`)(32,`span`,12),p(33,`Elapsed`),d(),l(34,`strong`),p(35),d()(),l(36,`mat-form-field`,14)(37,`mat-label`),p(38,`Objective`),d(),l(39,`mat-select`,15),C(`selectionChange`,function(o){return i.store.selectObjective(o.value)}),ve(40,wR,2,3,`mat-option`,16,Nb),d()()(),l(42,`section`,17)(43,`div`)(44,`span`),p(45,`Planned`),d(),l(46,`strong`),p(47),d()(),l(48,`div`)(49,`span`),p(50,`Pending`),d(),l(51,`strong`),p(52),d()(),l(53,`div`)(54,`span`),p(55,`Running`),d(),l(56,`strong`),p(57),d()(),l(58,`div`)(59,`span`),p(60,`Successful`),d(),l(61,`strong`),p(62),d()(),l(63,`div`)(64,`span`),p(65,`Failed`),d(),l(66,`strong`),p(67),d()(),l(68,`div`)(69,`span`),p(70,`Cancelled`),d(),l(71,`strong`),p(72),d()()(),l(73,`section`,18)(74,`div`,19)(75,`div`,20)(76,`span`),p(77,`Instances · execution`),d(),l(78,`span`),p(79),d()(),z(80,`mat-progress-bar`,21),d(),l(81,`div`,19)(82,`div`,20)(83,`span`),p(84,`Algorithms · selected instance`),d(),l(85,`span`),p(86),d()(),z(87,`mat-progress-bar`,22),d(),l(88,`div`,19)(89,`div`,20)(90,`span`),p(91),d(),l(92,`span`),p(93),d()(),z(94,`mat-progress-bar`,23),d()(),l(95,`p`,24),p(96),N(97,ER,2,4),d(),N(98,DR,2,1,`p`,24),N(99,kR,2,1,`button`,25),d()(),l(100,`mat-tab-group`,26),C(`selectedIndexChange`,function(o){return i.store.selectTab(o)}),l(101,`mat-tab`,27),at(102,UR,40,9,`ng-template`,28),d(),l(103,`mat-tab`,29),at(104,YR,63,11,`ng-template`,28),d(),l(105,`mat-tab`,30),at(106,JR,19,6,`ng-template`,28),d(),l(107,`mat-tab`,31),at(108,iP,19,3,`ng-template`,28),d(),l(109,`mat-tab`,32),at(110,oP,17,5,`ng-template`,28),d()(),N(111,rP,5,1,`mat-card`,9),N(112,lP,10,3,`mat-card`,33),d()),e&2){let a,o,r,s,c,m=mt(11);h(7),w(i.store.connectionLabel()),h(),S(`matMenuTriggerFor`,m),h(4),ye(i.panels),h(5),T((a=i.store.latestError())?17:-1,a);let u=i.store.status();h(8),w(i.phaseLabel(u?.phase??`IDLE`)),h(5),w(u?.currentExperiment??`Waiting for execution`),h(5),w(i.elapsedLabel(u?.elapsedMillis??0)),h(4),S(`value`,i.store.selectedObjective()),h(),ye(i.store.objectives()),h(7),w(u?.counts?.planned??0),h(5),w(u?.counts?.pending??0),h(5),w(u?.counts?.running??0),h(5),w(u?.counts?.succeeded??0),h(5),w(u?.counts?.failed??0),h(5),w(u?.counts?.cancelled??0);let g=i.store.progress();h(7),Ue(``,g.instances.completed,` / `,g.instances.total),h(),S(`value`,i.progressValue(g.instances.completed,g.instances.total)),h(6),Ue(``,g.algorithms.completed,` / `,g.algorithms.total),h(),S(`value`,i.progressValue(g.algorithms.completed,g.algorithms.total)),h(4),H(`Repetitions · `,g.repetitionScope),h(2),Ue(``,g.repetitions.completed,` / `,g.repetitions.total),h(),S(`value`,i.progressValue(g.repetitions.completed,g.repetitions.total)),h(2),Ue(` `,u?.runningInstances??0,` active instances · `,u?.runningAlgorithms??0,` active algorithms. Completed repetitions include failures; cancelled work does not count as completed. `),h(),T((o=i.store.lastUpdated())?97:-1,o),h(),T((r=i.store.estimatedRemainingMillis())?98:-1,r),h(),T(i.store.unseenIssues()>0?99:-1),h(),S(`selectedIndex`,i.store.tab()),h(11),T((s=i.store.resultError())?111:-1,s),h(),T((c=i.store.inspectedResult())?112:-1,c)}},dependencies:[Ti,ss,Tt,Zt,jn,Vn,Bn,Qu,Lc,Ku,Xu,Vc,di,Mi,ta,Yr,qr,ns,Ni,sa,ts,cm,$c,as,is,Ra,Oa,Hn,dm,Gc,Wc,lm,mm,um,fm,Qi,Sn],styles:[`[_nghost-%COMP%]{display:block;min-height:100vh;color:var(--%NS%mat-sys-on-surface);background:var(--%NS%mat-sys-surface-container-lowest)}.topbar[_ngcontent-%COMP%]{position:sticky;top:0;z-index:10;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);background:var(--%NS%mat-sys-surface);gap:.75rem}.logo[_ngcontent-%COMP%]{width:2.2rem;height:2.2rem}.brand[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-title-large);font-weight:700}.toolbar-spacer[_ngcontent-%COMP%]{flex:1}main[_ngcontent-%COMP%]{width:min(100% - 2rem,96rem);margin:auto;padding:1.5rem 0 4rem}.status-card[_ngcontent-%COMP%], .browser[_ngcontent-%COMP%], .issue[_ngcontent-%COMP%], .result-detail[_ngcontent-%COMP%]{margin:1rem 0}.summary[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(3,minmax(0,1fr)) minmax(13rem,1fr);gap:1rem;align-items:center}.summary[_ngcontent-%COMP%] > div[_ngcontent-%COMP%], .metrics[_ngcontent-%COMP%] > div[_ngcontent-%COMP%]{display:grid;gap:.3rem}.phase[_ngcontent-%COMP%]{text-transform:capitalize}.eyebrow[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-label-medium);text-transform:uppercase;color:var(--%NS%mat-sys-on-surface-variant);letter-spacing:.05em}.metrics[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(6,1fr);gap:1rem;padding:1.2rem 0}.metrics[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-headline-small)}.progress-grid[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:1.5rem;border-top:1px solid var(--%NS%mat-sys-outline-variant);padding-top:1rem}.progress-item[_ngcontent-%COMP%]{display:grid;gap:.5rem}.progress-label[_ngcontent-%COMP%]{display:flex;justify-content:space-between;gap:.5rem;font:var(--%NS%mat-sys-label-large)}.status-footnote[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font-size:.85rem;line-height:1.5}.filters[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:.75rem;align-items:center;margin:1rem 0}.filters[_ngcontent-%COMP%]   mat-form-field[_ngcontent-%COMP%]{flex:1;min-width:12rem}.table-scroll[_ngcontent-%COMP%]{overflow-x:auto}table[_ngcontent-%COMP%]{width:100%;border-collapse:collapse;font:var(--%NS%mat-sys-body-medium)}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{text-align:left;padding:.65rem .75rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);white-space:nowrap}th[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font:var(--%NS%mat-sys-label-large)}tr.selected[_ngcontent-%COMP%]{background:var(--%NS%mat-sys-secondary-container)}.chart-grid[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:1rem;margin-top:1rem}.detail-title[_ngcontent-%COMP%]{margin:1.5rem 0 1rem}small[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font-size:.85rem}.error-card[_ngcontent-%COMP%]{background:var(--%NS%mat-sys-error-container);color:var(--%NS%mat-sys-on-error-container);margin:1rem 0}.result-heading[_ngcontent-%COMP%]{display:flex;justify-content:space-between;align-items:center}.empty-state[_ngcontent-%COMP%]{min-height:8rem;display:grid;place-items:center;color:var(--%NS%mat-sys-on-surface-variant);text-align:center}.sr-only[_ngcontent-%COMP%]{position:absolute;width:1px;height:1px;padding:0;overflow:hidden;clip:rect(0,0,0,0)}@media(max-width:60rem){.summary[_ngcontent-%COMP%]{grid-template-columns:repeat(2,minmax(0,1fr))}.metrics[_ngcontent-%COMP%]{grid-template-columns:repeat(3,1fr)}.progress-grid[_ngcontent-%COMP%]{grid-template-columns:1fr}}@media(max-width:42rem){main[_ngcontent-%COMP%]{width:calc(100% - 1rem);padding-top:.25rem}.chart-grid[_ngcontent-%COMP%], .summary[_ngcontent-%COMP%]{grid-template-columns:1fr}.brand[_ngcontent-%COMP%]{display:none}.topbar[_ngcontent-%COMP%]{gap:.25rem;padding:0 .5rem}.logo[_ngcontent-%COMP%]{width:1.5rem}.filters[_ngcontent-%COMP%]   mat-form-field[_ngcontent-%COMP%]{min-width:100%}}`]})};var dP=new D(`MAT_BADGE_CONFIG`);var Fw=`mat-badge-content`;var uP=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵcmp=B({type:t,selectors:[[`ng-component`]],decls:0,vars:0,template:function(i,a){},styles:[`.mat-badge {
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
`],encapsulation:2})}return t})();var Lw=(()=>{class t{_ngZone=f(K);_elementRef=f(G);_ariaDescriber=f(ku);_renderer=f(Ae);_animationsDisabled=ze();_idGenerator=f(Le);get color(){return this._color}set color(e){this._setColor(e),this._color=e}_color;overlap;disabled=!1;position;get content(){return this._content}set content(e){this._updateRenderedContent(e)}_content;get description(){return this._description}set description(e){this._updateDescription(e)}_description;size;hidden=!1;_badgeElement;_inlineBadgeDescription;_isInitialized=!1;_interactivityChecker=f(Rr);_document=f(ce);constructor(){let e=f(dP,{optional:!0}),i=f(Ze);i.load(uP),i.load(Di),this._color=e?.color||`primary`,this.overlap=e?.overlap??!0,this.position=e?.position||`above after`,this.size=e?.size||`medium`}isAbove(){return this.position.indexOf(`below`)===-1}isAfter(){return this.position.indexOf(`before`)===-1}getBadgeElement(){return this._badgeElement}ngOnInit(){this._clearExistingBadges(),this.content&&!this._badgeElement&&(this._badgeElement=this._createBadgeElement(),this._updateRenderedContent(this.content)),this._isInitialized=!0}ngAfterViewInit(){}ngOnDestroy(){this._renderer.destroyNode&&(this._renderer.destroyNode(this._badgeElement),this._inlineBadgeDescription?.remove()),this._ariaDescriber.removeDescription(this._elementRef.nativeElement,this.description)}_isHostInteractive(){return this._interactivityChecker.isFocusable(this._elementRef.nativeElement,{ignoreVisibility:!0})}_createBadgeElement(){let e=this._renderer.createElement(`span`),i=`mat-badge-active`;return e.setAttribute(`id`,this._idGenerator.getId(`mat-badge-content-`)),e.setAttribute(`aria-hidden`,`true`),e.classList.add(Fw),this._animationsDisabled&&e.classList.add(`_mat-animation-noopable`),this._elementRef.nativeElement.appendChild(e),typeof requestAnimationFrame==`function`&&!this._animationsDisabled?this._ngZone.runOutsideAngular(()=>{requestAnimationFrame(()=>{e.classList.add(i)})}):e.classList.add(i),e}_updateRenderedContent(e){let i=`${e??``}`.trim();this._isInitialized&&i&&!this._badgeElement&&(this._badgeElement=this._createBadgeElement()),this._badgeElement&&(this._badgeElement.textContent=i),this._content=i}_updateDescription(e){this._ariaDescriber.removeDescription(this._elementRef.nativeElement,this.description),(!e||this._isHostInteractive())&&this._removeInlineDescription(),this._description=e,this._isHostInteractive()?this._ariaDescriber.describe(this._elementRef.nativeElement,e):this._updateInlineDescription()}_updateInlineDescription(){this._inlineBadgeDescription||(this._inlineBadgeDescription=this._document.createElement(`span`),this._inlineBadgeDescription.classList.add(`cdk-visually-hidden`)),this._inlineBadgeDescription.textContent=this.description,this._badgeElement?.appendChild(this._inlineBadgeDescription)}_removeInlineDescription(){this._inlineBadgeDescription?.remove(),this._inlineBadgeDescription=void 0}_setColor(e){let i=this._elementRef.nativeElement.classList;i.remove(`mat-badge-${this._color}`),e&&i.add(`mat-badge-${e}`)}_clearExistingBadges(){let e=this._elementRef.nativeElement.querySelectorAll(`:scope > .${Fw}`);for(let i of Array.from(e))i!==this._badgeElement&&i.remove()}static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,selectors:[[``,`matBadge`,``]],hostAttrs:[1,`mat-badge`],hostVars:20,hostBindings:function(i,a){i&2&&W(`mat-badge-overlap`,a.overlap)(`mat-badge-above`,a.isAbove())(`mat-badge-below`,!a.isAbove())(`mat-badge-before`,!a.isAfter())(`mat-badge-after`,a.isAfter())(`mat-badge-small`,a.size===`small`)(`mat-badge-medium`,a.size===`medium`)(`mat-badge-large`,a.size===`large`)(`mat-badge-hidden`,a.hidden||!a.content)(`mat-badge-disabled`,a.disabled)},inputs:{color:[0,`matBadgeColor`,`color`],overlap:[2,`matBadgeOverlap`,`overlap`,V],disabled:[2,`matBadgeDisabled`,`disabled`,V],position:[0,`matBadgePosition`,`position`],content:[0,`matBadge`,`content`],description:[0,`matBadgeDescription`,`description`],size:[0,`matBadgeSize`,`size`],hidden:[2,`matBadgeHidden`,`hidden`,V]}})}return t})();var Vw=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({imports:[Do,we]})}return t})();var Va=class{viewContainerRef;injector;id;role=`dialog`;panelClass=``;hasBackdrop=!0;backdropClass=``;disableClose=!1;closePredicate;width=``;height=``;minWidth;minHeight;maxWidth;maxHeight;positionStrategy;data=null;direction;ariaDescribedBy=null;ariaLabelledBy=null;ariaLabel=null;ariaModal=!1;autoFocus=`first-tabbable`;restoreFocus=!0;scrollStrategy;closeOnNavigation=!0;closeOnDestroy=!0;closeOnOverlayDetachments=!0;disableAnimations=!1;providers;container;templateContext;bindings};var Ab=(()=>{class t extends Kr{_elementRef=f(G);_focusTrapFactory=f(Ag);_config;_interactivityChecker=f(Rr);_ngZone=f(K);_focusMonitor=f(Xt);_renderer=f(Ae);_changeDetectorRef=f(Re);_injector=f(re);_platform=f(Te);_document=f(ce);_portalOutlet;_focusTrapped=new L;_focusTrap=null;_elementFocusedBeforeDialogWasOpened=null;_closeInteractionType=null;_ariaLabelledByQueue=[];_isDestroyed=!1;constructor(){super(),this._config=f(Va,{optional:!0})||new Va,this._config.ariaLabelledBy&&this._ariaLabelledByQueue.push(this._config.ariaLabelledBy)}_addAriaLabelledBy(e){this._ariaLabelledByQueue.push(e),this._changeDetectorRef.markForCheck()}_removeAriaLabelledBy(e){let i=this._ariaLabelledByQueue.indexOf(e);i>-1&&(this._ariaLabelledByQueue.splice(i,1),this._changeDetectorRef.markForCheck())}_contentAttached(){this._initializeFocusTrap(),this._captureInitialFocus()}_captureInitialFocus(){this._trapFocus()}ngOnDestroy(){this._focusTrapped.complete(),this._isDestroyed=!0,this._restoreFocus()}attachComponentPortal(e){this._portalOutlet.hasAttached();let i=this._portalOutlet.attachComponentPortal(e);return this._contentAttached(),i}attachTemplatePortal(e){this._portalOutlet.hasAttached();let i=this._portalOutlet.attachTemplatePortal(e);return this._contentAttached(),i}attachDomPortal=e=>{this._portalOutlet.hasAttached();let i=this._portalOutlet.attachDomPortal(e);return this._contentAttached(),i};_recaptureFocus(){this._containsFocus()||this._trapFocus()}_forceFocus(e,i){this._interactivityChecker.isFocusable(e)||(e.tabIndex=-1,this._ngZone.runOutsideAngular(()=>{let a=()=>{o(),r(),e.removeAttribute(`tabindex`)},o=this._renderer.listen(e,`blur`,a),r=this._renderer.listen(e,`mousedown`,a)})),e.focus(i)}_focusByCssSelector(e,i){let a=this._elementRef.nativeElement.querySelector(e);a&&this._forceFocus(a,i)}_trapFocus(e){this._isDestroyed||Mt(()=>{let i=this._elementRef.nativeElement;switch(this._config.autoFocus){case!1:case`dialog`:this._containsFocus()||i.focus(e);break;case!0:case`first-tabbable`:this._focusTrap?.focusInitialElement(e)||this._focusDialogContainer(e);break;case`first-heading`:this._focusByCssSelector(`h1, h2, h3, h4, h5, h6, [role="heading"]`,e);break;default:this._focusByCssSelector(this._config.autoFocus,e)}this._focusTrapped.next()},{injector:this._injector})}_restoreFocus(){let e=this._config.restoreFocus,i=null;if(typeof e==`string`?i=this._document.querySelector(e):typeof e==`boolean`?i=e?this._elementFocusedBeforeDialogWasOpened:null:e&&(i=e),this._config.restoreFocus&&i&&typeof i.focus==`function`){let a=_c(),o=this._elementRef.nativeElement;(!a||a===this._document.body||a===o||o.contains(a))&&(this._focusMonitor?(this._focusMonitor.focusVia(i,this._closeInteractionType),this._closeInteractionType=null):i.focus())}this._focusTrap&&this._focusTrap.destroy()}_focusDialogContainer(e){this._elementRef.nativeElement.focus?.(e)}_containsFocus(){let e=this._elementRef.nativeElement,i=_c();return e===i||e.contains(i)}_initializeFocusTrap(){this._platform.isBrowser&&(this._focusTrap=this._focusTrapFactory.create(this._elementRef.nativeElement),this._document&&(this._elementFocusedBeforeDialogWasOpened=_c()))}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){function e(i,a){}return B({type:t,selectors:[[`cdk-dialog-container`]],viewQuery:function(a,o){if(a&1&&Xe(Ta,7),a&2){let r;X(r=Z())&&(o._portalOutlet=r.first)}},hostAttrs:[`tabindex`,`-1`,1,`cdk-dialog-container`],hostVars:6,hostBindings:function(a,o){a&2&&ee(`id`,o._config.id||null)(`role`,o._config.role)(`aria-modal`,o._config.ariaModal)(`aria-labelledby`,o._config.ariaLabel?null:o._ariaLabelledByQueue[0])(`aria-label`,o._config.ariaLabel)(`aria-describedby`,o._config.ariaDescribedBy||null)},features:[Fe],decls:1,vars:0,consts:[[`cdkPortalOutlet`,``]],template:function(a,o){a&1&&at(0,e,0,0,`ng-template`,0)},dependencies:[Ta],styles:[`.cdk-dialog-container {
  display: block;
  width: 100%;
  height: 100%;
  min-height: inherit;
  max-height: inherit;
}
`],encapsulation:2,changeDetection:1})})()}return t})();var Vo=class{overlayRef;config;componentInstance=null;componentRef=null;containerInstance;disableClose;closed=new L;backdropClick;keydownEvents;outsidePointerEvents;id;_detachSubscription;constructor(n,e){this.overlayRef=n,this.config=e,this.disableClose=e.disableClose,this.backdropClick=n.backdropClick(),this.keydownEvents=n.keydownEvents(),this.outsidePointerEvents=n.outsidePointerEvents(),this.id=e.id,this.keydownEvents.subscribe(i=>{i.keyCode===27&&!this.disableClose&&!vt(i)&&(i.preventDefault(),this.close(void 0,{focusOrigin:`keyboard`}))}),this.backdropClick.subscribe(()=>{!this.disableClose&&this._canClose()?this.close(void 0,{focusOrigin:`mouse`}):this.containerInstance._recaptureFocus?.()}),this._detachSubscription=n.detachments().subscribe(()=>{e.closeOnOverlayDetachments!==!1&&this.close()})}close(n,e){if(this._canClose(n)){let i=this.closed;this.containerInstance._closeInteractionType=e?.focusOrigin||`program`,this._detachSubscription.unsubscribe(),this.overlayRef.dispose(),i.next(n),i.complete(),this.componentInstance=this.containerInstance=null}}updatePosition(){return this.overlayRef.updatePosition(),this}updateSize(n=``,e=``){return this.overlayRef.updateSize({width:n,height:e}),this}addPanelClass(n){return this.overlayRef.addPanelClass(n),this}removePanelClass(n){return this.overlayRef.removePanelClass(n),this}_canClose(n){let e=this.config;return!!this.containerInstance&&(!e.closePredicate||e.closePredicate(n,e,this.componentInstance))}};var pP=new D(`DialogScrollStrategy`,{providedIn:`root`,factory:()=>{let t=f(re);return()=>Zr(t)}});var hP=new D(`DialogData`);var fP=new D(`DefaultDialogConfig`);function gP(t){let n=M(t),e=new J;return{valueSignal:n,get value(){return n()},change:e,ngOnDestroy(){e.complete()}}}var Ob=(()=>{class t{_injector=f(re);_defaultOptions=f(fP,{optional:!0});_parentDialog=f(t,{optional:!0,skipSelf:!0});_overlayContainer=f(am);_idGenerator=f(Le);_openDialogsAtThisLevel=[];_afterAllClosedAtThisLevel=new L;_afterOpenedAtThisLevel=new L;_ariaHiddenElements=new Map;_scrollStrategy=f(pP);get openDialogs(){return this._parentDialog?this._parentDialog.openDialogs:this._openDialogsAtThisLevel}get afterOpened(){return this._parentDialog?this._parentDialog.afterOpened:this._afterOpenedAtThisLevel}afterAllClosed=Ya(()=>this.openDialogs.length?this._getAfterAllClosed():this._getAfterAllClosed().pipe(dt(void 0)));open(e,i){let a=this._defaultOptions||new Va;i=r(r({},a),i),i.id=i.id||this._idGenerator.getId(`cdk-dialog-`),i.id&&this.getDialogById(i.id);let o=this._getOverlayConfig(i),r$3=ra(this._injector,o),s=new Vo(r$3,i),c=this._attachContainer(r$3,s,i);if(s.containerInstance=c,!this.openDialogs.length){let m=this._overlayContainer.getContainerElement();c._focusTrapped?c._focusTrapped.pipe(hn(1)).subscribe(()=>{this._hideNonDialogContentFromAssistiveTechnology(m)}):this._hideNonDialogContentFromAssistiveTechnology(m)}return this._attachDialogContent(e,s,c,i),this.openDialogs.push(s),s.closed.subscribe(()=>this._removeOpenDialog(s,!0)),this.afterOpened.next(s),s}closeAll(){Tb(this.openDialogs,e=>e.close())}getDialogById(e){return this.openDialogs.find(i=>i.id===e)}ngOnDestroy(){Tb(this._openDialogsAtThisLevel,e=>{e.config.closeOnDestroy===!1&&this._removeOpenDialog(e,!1)}),Tb(this._openDialogsAtThisLevel,e=>e.close()),this._afterAllClosedAtThisLevel.complete(),this._afterOpenedAtThisLevel.complete(),this._openDialogsAtThisLevel=[]}_getOverlayConfig(e){let i=new ia({positionStrategy:e.positionStrategy||Jr().centerHorizontally().centerVertically(),scrollStrategy:e.scrollStrategy||this._scrollStrategy(),panelClass:e.panelClass,hasBackdrop:e.hasBackdrop,direction:e.direction,minWidth:e.minWidth,minHeight:e.minHeight,maxWidth:e.maxWidth,maxHeight:e.maxHeight,width:e.width,height:e.height,disposeOnNavigation:e.closeOnNavigation,disableAnimations:e.disableAnimations});return e.backdropClass&&(i.backdropClass=e.backdropClass),i}_attachContainer(e,i,a){let o=a.injector||a.viewContainerRef?.injector,r=[{provide:Va,useValue:a},{provide:Vo,useValue:i},{provide:Qr,useValue:e}],s;a.container?typeof a.container==`function`?s=a.container:(s=a.container.type,r.push(...a.container.providers(a))):s=Ab;let c=new Na(s,a.viewContainerRef,re.create({parent:o||this._injector,providers:r}));return e.attach(c).instance}_attachDialogContent(e,i,a,o){if(e instanceof Ft){let r$4=this._createInjector(o,i,a,void 0),s={$implicit:o.data,dialogRef:i};o.templateContext&&(s=r(r({},s),typeof o.templateContext==`function`?o.templateContext():o.templateContext)),a.attachTemplatePortal(new zn(e,null,s,r$4))}else{let r=this._createInjector(o,i,a,this._injector),s=a.attachComponentPortal(new Na(e,o.viewContainerRef,r,null,o.bindings));i.componentRef=s,i.componentInstance=s.instance}}_createInjector(e,i,a,o){let r=e.injector||e.viewContainerRef?.injector,s=[{provide:hP,useValue:e.data},{provide:Vo,useValue:i}];return e.providers&&(typeof e.providers==`function`?s.push(...e.providers(i,e,a)):s.push(...e.providers)),e.direction&&(!r||!r.get(pt,null,{optional:!0}))&&s.push({provide:pt,useValue:gP(e.direction)}),re.create({parent:r||o,providers:s})}_removeOpenDialog(e,i){let a=this.openDialogs.indexOf(e);a>-1&&(this.openDialogs.splice(a,1),this.openDialogs.length||(this._ariaHiddenElements.forEach((o,r)=>{o?r.setAttribute(`aria-hidden`,o):r.removeAttribute(`aria-hidden`)}),this._ariaHiddenElements.clear(),i&&this._getAfterAllClosed().next()))}_hideNonDialogContentFromAssistiveTechnology(e){if(e.parentElement){let i=e.parentElement.children;for(let a=i.length-1;a>-1;a--){let o=i[a];o!==e&&o.nodeName!==`SCRIPT`&&o.nodeName!==`STYLE`&&!o.hasAttribute(`aria-live`)&&!o.hasAttribute(`popover`)&&(this._ariaHiddenElements.set(o,o.getAttribute(`aria-hidden`)),o.setAttribute(`aria-hidden`,`true`))}}}_getAfterAllClosed(){let e=this._parentDialog;return e?e._getAfterAllClosed():this._afterAllClosedAtThisLevel}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();function Tb(t,n){let e=t.length;for(;e--;)n(t[e])}var Bw=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({providers:[Ob],imports:[mi,Ro,Do,Ro]})}return t})();var vm=class{viewContainerRef;injector;id;role=`dialog`;panelClass=``;hasBackdrop=!0;backdropClass=``;disableClose=!1;closePredicate;width=``;height=``;minWidth;minHeight;maxWidth;maxHeight;position;data=null;direction;ariaDescribedBy=null;ariaLabelledBy=null;ariaLabel=null;ariaModal=!1;autoFocus=`first-tabbable`;restoreFocus=!0;delayFocusTrap=!0;scrollStrategy;closeOnNavigation=!0;enterAnimationDuration;exitAnimationDuration;bindings};var Rb=`mdc-dialog--open`;var jw=`mdc-dialog--opening`;var zw=`mdc-dialog--closing`;var bP=150;var vP=75;var yP=(()=>{class t extends Ab{_animationStateChanged=new J;_animationsEnabled=!ze();_actionSectionCount=0;_hostElement=this._elementRef.nativeElement;_enterAnimationDuration=this._animationsEnabled?Uw(this._config.enterAnimationDuration)??bP:0;_exitAnimationDuration=this._animationsEnabled?Uw(this._config.exitAnimationDuration)??vP:0;_animationTimer=null;_contentAttached(){super._contentAttached(),this._startOpenAnimation()}_startOpenAnimation(){this._animationStateChanged.emit({state:`opening`,totalTime:this._enterAnimationDuration}),this._animationsEnabled?(this._hostElement.style.setProperty(Hw,`${this._enterAnimationDuration}ms`),this._requestAnimationFrame(()=>this._hostElement.classList.add(jw,Rb)),this._waitForAnimationToComplete(this._enterAnimationDuration,this._finishDialogOpen)):(this._hostElement.classList.add(Rb),Promise.resolve().then(()=>this._finishDialogOpen()))}_startExitAnimation(){this._animationStateChanged.emit({state:`closing`,totalTime:this._exitAnimationDuration}),this._hostElement.classList.remove(Rb),this._animationsEnabled?(this._hostElement.style.setProperty(Hw,`${this._exitAnimationDuration}ms`),this._requestAnimationFrame(()=>this._hostElement.classList.add(zw)),this._waitForAnimationToComplete(this._exitAnimationDuration,this._finishDialogClose)):Promise.resolve().then(()=>this._finishDialogClose())}_updateActionSectionCount(e){this._actionSectionCount+=e,this._changeDetectorRef.markForCheck()}_finishDialogOpen=()=>{this._clearAnimationClasses(),this._openAnimationDone(this._enterAnimationDuration)};_finishDialogClose=()=>{this._clearAnimationClasses(),this._animationStateChanged.emit({state:`closed`,totalTime:this._exitAnimationDuration})};_clearAnimationClasses(){this._hostElement.classList.remove(jw,zw)}_waitForAnimationToComplete(e,i){this._animationTimer!==null&&clearTimeout(this._animationTimer),this._animationTimer=setTimeout(i,e)}_requestAnimationFrame(e){this._ngZone.runOutsideAngular(()=>{typeof requestAnimationFrame==`function`?requestAnimationFrame(e):e()})}_captureInitialFocus(){this._config.delayFocusTrap||this._trapFocus()}_openAnimationDone(e){this._config.delayFocusTrap&&this._trapFocus(),this._animationStateChanged.next({state:`opened`,totalTime:e})}ngOnDestroy(){super.ngOnDestroy(),this._animationTimer!==null&&clearTimeout(this._animationTimer)}attachComponentPortal(e){let i=super.attachComponentPortal(e);return i.location.nativeElement.classList.add(`mat-mdc-dialog-component-host`),i}static ɵfac=(()=>{let e;return function(a){return(e||(e=It(t)))(a||t)}})();static ɵcmp=(function(){function e(i,a){}return B({type:t,selectors:[[`mat-dialog-container`]],hostAttrs:[`tabindex`,`-1`,1,`mat-mdc-dialog-container`,`mdc-dialog`],hostVars:10,hostBindings:function(a,o){a&2&&(Wt(`id`,o._config.id),ee(`aria-modal`,o._config.ariaModal)(`role`,o._config.role)(`aria-labelledby`,o._config.ariaLabel?null:o._ariaLabelledByQueue[0])(`aria-label`,o._config.ariaLabel)(`aria-describedby`,o._config.ariaDescribedBy||null),W(`_mat-animation-noopable`,!o._animationsEnabled)(`mat-mdc-dialog-container-with-actions`,o._actionSectionCount>0))},features:[Fe],decls:3,vars:0,consts:[[1,`mat-mdc-dialog-inner-container`,`mdc-dialog__container`],[1,`mat-mdc-dialog-surface`,`mdc-dialog__surface`],[`cdkPortalOutlet`,``]],template:function(a,o){a&1&&(l(0,`div`,0)(1,`div`,1),at(2,e,0,0,`ng-template`,2),d()())},dependencies:[Ta],styles:[`.mat-mdc-dialog-container {
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
`],encapsulation:2,changeDetection:1})})()}return t})();var Hw=`--mat-dialog-transition-duration`;function Uw(t){return t==null?null:typeof t==`number`?t:t.endsWith(`ms`)?ri(t.substring(0,t.length-2)):t.endsWith(`s`)?ri(t.substring(0,t.length-1))*1e3:t===`0`?0:null}var bm=(function(t){return t[t.OPEN=0]=`OPEN`,t[t.CLOSING=1]=`CLOSING`,t[t.CLOSED=2]=`CLOSED`,t})(bm||{});var Kc=class{_ref;_config;_containerInstance;componentInstance;componentRef=null;disableClose;id;_afterOpened=new qn(1);_beforeClosed=new qn(1);_result;_closeFallbackTimeout;_state=bm.OPEN;_closeInteractionType;constructor(n,e,i){this._ref=n,this._config=e,this._containerInstance=i,this.disableClose=e.disableClose,this.id=n.id,n.addPanelClass(`mat-mdc-dialog-panel`),i._animationStateChanged.pipe(Ke(a=>a.state===`opened`),hn(1)).subscribe(()=>{this._afterOpened.next(),this._afterOpened.complete()}),i._animationStateChanged.pipe(Ke(a=>a.state===`closed`),hn(1)).subscribe(()=>{clearTimeout(this._closeFallbackTimeout),this._finishDialogClose()}),n.overlayRef.detachments().subscribe(()=>{this._beforeClosed.next(this._result),this._beforeClosed.complete(),this._finishDialogClose()}),wt(this.backdropClick(),this.keydownEvents().pipe(Ke(a=>a.keyCode===27&&!this.disableClose&&!vt(a)))).subscribe(a=>{this.disableClose||(a.preventDefault(),$w(this,a.type===`keydown`?`keyboard`:`mouse`))})}close(n){let e=this._config.closePredicate;e&&!e(n,this._config,this.componentInstance)||(this._result=n,this._containerInstance._animationStateChanged.pipe(Ke(i=>i.state===`closing`),hn(1)).subscribe(i=>{this._beforeClosed.next(n),this._beforeClosed.complete(),this._ref.overlayRef.detachBackdrop(),this._closeFallbackTimeout=setTimeout(()=>this._finishDialogClose(),i.totalTime+100)}),this._state=bm.CLOSING,this._containerInstance._startExitAnimation())}afterOpened(){return this._afterOpened}afterClosed(){return this._ref.closed}beforeClosed(){return this._beforeClosed}backdropClick(){return this._ref.backdropClick}keydownEvents(){return this._ref.keydownEvents}updatePosition(n){let e=this._ref.config.positionStrategy;return n&&(n.left||n.right)?n.left?e.left(n.left):e.right(n.right):e.centerHorizontally(),n&&(n.top||n.bottom)?n.top?e.top(n.top):e.bottom(n.bottom):e.centerVertically(),this._ref.updatePosition(),this}updateSize(n=``,e=``){return this._ref.updateSize(n,e),this}addPanelClass(n){return this._ref.addPanelClass(n),this}removePanelClass(n){return this._ref.removePanelClass(n),this}getState(){return this._state}_finishDialogClose(){this._state=bm.CLOSED,this._ref.close(this._result,{focusOrigin:this._closeInteractionType}),this.componentInstance=null}};function $w(t,n,e){return t._closeInteractionType=n,t.close(e)}var Qc=new D(`MatMdcDialogData`);var _P=new D(`mat-mdc-dialog-default-options`);var xP=new D(`mat-mdc-dialog-scroll-strategy`,{providedIn:`root`,factory:()=>{let t=f(re);return()=>Zr(t)}});var Bo=(()=>{class t{_defaultOptions=f(_P,{optional:!0});_scrollStrategy=f(xP);_parentDialog=f(t,{optional:!0,skipSelf:!0});_idGenerator=f(Le);_injector=f(re);_dialog=f(Ob);_animationsDisabled=ze();_openDialogsAtThisLevel=[];_afterAllClosedAtThisLevel=new L;_afterOpenedAtThisLevel=new L;dialogConfigClass=vm;_dialogRefConstructor;_dialogContainerType;_dialogDataToken;get openDialogs(){return this._parentDialog?this._parentDialog.openDialogs:this._openDialogsAtThisLevel}get afterOpened(){return this._parentDialog?this._parentDialog.afterOpened:this._afterOpenedAtThisLevel}_getAfterAllClosed(){let e=this._parentDialog;return e?e._getAfterAllClosed():this._afterAllClosedAtThisLevel}afterAllClosed=Ya(()=>this.openDialogs.length?this._getAfterAllClosed():this._getAfterAllClosed().pipe(dt(void 0)));constructor(){this._dialogRefConstructor=Kc,this._dialogContainerType=yP,this._dialogDataToken=Qc}open(e,i){let a;i=r(r({},this._defaultOptions||new vm),i),i.id=i.id||this._idGenerator.getId(`mat-mdc-dialog-`),i.scrollStrategy=i.scrollStrategy||this._scrollStrategy();let o=this._dialog.open(e,s(r({},i),{positionStrategy:Jr(this._injector).centerHorizontally().centerVertically(),disableClose:!0,closePredicate:void 0,closeOnDestroy:!1,closeOnOverlayDetachments:!1,disableAnimations:this._animationsDisabled||i.enterAnimationDuration?.toLocaleString()===`0`||i.exitAnimationDuration?.toString()===`0`,container:{type:this._dialogContainerType,providers:()=>[{provide:this.dialogConfigClass,useValue:i},{provide:Va,useValue:i}]},templateContext:()=>({dialogRef:a}),providers:(r,s,c)=>(a=new this._dialogRefConstructor(r,i,c),a.updatePosition(i?.position),[{provide:this._dialogContainerType,useValue:c},{provide:this._dialogDataToken,useValue:s.data},{provide:this._dialogRefConstructor,useValue:a},{provide:Vo,useValue:null}])}));return a.componentRef=o.componentRef,a.componentInstance=o.componentInstance,this.openDialogs.push(a),this.afterOpened.next(a),a.afterClosed().subscribe(()=>{let r=this.openDialogs.indexOf(a);r>-1&&(this.openDialogs.splice(r,1),this.openDialogs.length||this._getAfterAllClosed().next())}),a}closeAll(){this._closeDialogs(this.openDialogs)}getDialogById(e){return this.openDialogs.find(i=>i.id===e)}ngOnDestroy(){this._closeDialogs(this._openDialogsAtThisLevel),this._afterAllClosedAtThisLevel.complete(),this._afterOpenedAtThisLevel.complete()}_closeDialogs(e){let i=e.length;for(;i--;)e[i].close()}static ɵfac=function(i){return new(i||t)};static ɵprov=se({token:t,factory:t.ɵfac})}return t})();var ym=(()=>{class t{dialogRef=f(Kc,{optional:!0});_elementRef=f(G);_dialog=f(Bo);ariaLabel;type=`button`;dialogResult;_matDialogClose;ngOnInit(){this.dialogRef||(this.dialogRef=Ww(this._elementRef,this._dialog.openDialogs))}ngOnChanges(e){let i=e._matDialogClose;i&&(this.dialogResult=i.currentValue)}_onButtonClick(e){this._elementRef.nativeElement.getAttribute(`aria-disabled`)!==`true`&&$w(this.dialogRef,e.screenX===0&&e.screenY===0?`keyboard`:`mouse`,this.dialogResult)}static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,selectors:[[``,`mat-dialog-close`,``],[``,`matDialogClose`,``]],hostVars:2,hostBindings:function(i,a){i&1&&C(`click`,function(r){return a._onButtonClick(r)}),i&2&&ee(`aria-label`,a.ariaLabel||null)(`type`,a.type)},inputs:{ariaLabel:[0,`aria-label`,`ariaLabel`],type:`type`,dialogResult:[0,`mat-dialog-close`,`dialogResult`],_matDialogClose:[0,`matDialogClose`,`_matDialogClose`]},exportAs:[`matDialogClose`],features:[ft]})}return t})();var Gw=(()=>{class t{_dialogRef=f(Kc,{optional:!0});_elementRef=f(G);_dialog=f(Bo);ngOnInit(){this._dialogRef||(this._dialogRef=Ww(this._elementRef,this._dialog.openDialogs)),this._dialogRef&&Promise.resolve().then(()=>{this._onAdd()})}ngOnDestroy(){this._dialogRef?._containerInstance&&Promise.resolve().then(()=>{this._onRemove()})}static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t})}return t})();var _m=(()=>{class t extends Gw{id=f(Le).getId(`mat-mdc-dialog-title-`);_onAdd(){this._dialogRef._containerInstance?._addAriaLabelledBy?.(this.id)}_onRemove(){this._dialogRef?._containerInstance?._removeAriaLabelledBy?.(this.id)}static ɵfac=(()=>{let e;return function(a){return(e||(e=It(t)))(a||t)}})();static ɵdir=q({type:t,selectors:[[``,`mat-dialog-title`,``],[``,`matDialogTitle`,``]],hostAttrs:[1,`mat-mdc-dialog-title`,`mdc-dialog__title`],hostVars:1,hostBindings:function(i,a){i&2&&Wt(`id`,a.id)},inputs:{id:`id`},exportAs:[`matDialogTitle`],features:[Fe]})}return t})();var xm=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,selectors:[[``,`mat-dialog-content`,``],[`mat-dialog-content`],[``,`matDialogContent`,``]],hostAttrs:[1,`mat-mdc-dialog-content`,`mdc-dialog__content`],features:[tc([zc])]})}return t})();var Cm=(()=>{class t extends Gw{align;_onAdd(){this._dialogRef._containerInstance?._updateActionSectionCount?.(1)}_onRemove(){this._dialogRef._containerInstance?._updateActionSectionCount?.(-1)}static ɵfac=(()=>{let e;return function(a){return(e||(e=It(t)))(a||t)}})();static ɵdir=q({type:t,selectors:[[``,`mat-dialog-actions`,``],[`mat-dialog-actions`],[``,`matDialogActions`,``]],hostAttrs:[1,`mat-mdc-dialog-actions`,`mdc-dialog__actions`],hostVars:6,hostBindings:function(i,a){i&2&&W(`mat-mdc-dialog-actions-align-start`,a.align===`start`)(`mat-mdc-dialog-actions-align-center`,a.align===`center`)(`mat-mdc-dialog-actions-align-end`,a.align===`end`)},inputs:{align:`align`},features:[Fe]})}return t})();function Ww(t,n){let e=t.nativeElement.parentElement;for(;e&&!e.classList.contains(`mat-mdc-dialog-container`);)e=e.parentElement;return e?n.find(i=>i.id===e.id):null}var Sm=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({providers:[Bo],imports:[Bw,mi,Ro,we]})}return t})();var wm=class t{http=f(Ei);getStatus(){return At(this.http.get(this.endpoint(`/status`)))}getComponentUsage(n){return At(this.http.get(this.endpoint(`/components/usage`),{params:{scope:n}}))}getComponentCandidates(n){return At(this.http.get(this.endpoint(`/components/candidates`),{params:r({scope:n.scope,basis:n.basis,offset:n.offset,limit:n.limit},n.selector)}))}getEvaluationChanges(n,e){return At(this.http.get(this.endpoint(`/evaluations/changes`),{params:{after:n,limit:e}}))}getElites(){return At(this.http.get(this.endpoint(`/elites`)))}getEliteHistory(){return At(this.http.get(this.endpoint(`/elites/history`)))}getCandidate(n){return At(this.http.get(this.endpoint(`/candidates/${encodeURIComponent(n)}`)))}getSearchSpace(){return At(this.http.get(this.endpoint(`/search-space`)))}getArtifacts(){return At(this.http.get(this.endpoint(`/artifacts`)))}artifactUrl(n){return this.endpoint(`/artifacts/${encodeURIComponent(n)}`)}endpoint(n){return`${Dn.apiBaseUrl}/api/autoconfig${n}`}static ɵfac=function(e){return new(e||t)};static ɵprov=he({token:t,factory:t.ɵfac,providedIn:`root`})};var jo=class{static evaluationCost(n){let e=[],i=[];for(let a of n){if(a.state!==`SUCCEEDED`||typeof a.cost!=`number`||!Number.isFinite(a.cost))continue;let o={x:a.id,y:a.cost,custom:{configurationId:a.configurationId,instanceName:a.instanceName,timeSeconds:a.timeSeconds}};(a.slow?i:e).push(o)}return{options:this.baseOptions(`Evaluation cost`,`Successful IRACE costs. Lower is better.`,{allowDecimals:!1,title:{text:`Evaluation ID`}},{title:{text:`IRACE cost`}},[{type:`scatter`,name:`Succeeded`,data:e,color:`#00aab5`,marker:{radius:2.5},turboThreshold:0},{type:`scatter`,name:`Slow`,data:i,color:`#ed8e00`,marker:{radius:3.5,symbol:`diamond`},turboThreshold:0}],`<b>{series.name}</b><br/>Evaluation: {point.x}<br/>Cost: {point.y}<br/>Configuration: {point.custom.configurationId}<br/>Instance: {point.custom.instanceName}`)}}static evaluationActivity(n){let e=n.filter(s=>s.finishedAt&&s.state!==`RUNNING`).slice().sort((s,c)=>{let m=Date.parse(s.finishedAt)-Date.parse(c.finishedAt);return m===0?s.id-c.id:m}),i={SUCCEEDED:0,REJECTED:0,FAILED:0},a=[],o=[],r=[];for(let s of e){let c=s.state;if(c!==`SUCCEEDED`&&c!==`REJECTED`&&c!==`FAILED`)continue;i[c]++;let m=[Date.parse(s.finishedAt),i[c]];c===`SUCCEEDED`?a.push(m):c===`REJECTED`?o.push(m):r.push(m)}return{options:this.baseOptions(`Evaluation activity`,`Cumulative terminal evaluations over wall-clock time.`,{type:`datetime`,title:{text:`Completion time`}},{allowDecimals:!1,min:0,title:{text:`Evaluations`}},[{type:`line`,name:`Succeeded`,data:a,color:`#079447`,marker:{enabled:!1}},{type:`line`,name:`Rejected`,data:o,color:`#ed8e00`,marker:{enabled:!1}},{type:`line`,name:`Failed`,data:r,color:`#d32f2f`,marker:{enabled:!1}}],`<b>{series.name}</b><br/>{point.x:%Y-%m-%d %H:%M:%S}<br/>Count: {point.y}`)}}static eliteRanks(n){let e=n?.iterations??[],i=[];for(let o of e)for(let r of o.elites)i.includes(r.configurationId)||i.push(r.configurationId);let a=[];for(let o of i){let r=[];for(let s of e){let c=null;for(let m of s.elites)if(m.configurationId===o){c=m.position;break}r.push([s.iteration,c])}a.push({type:`line`,name:o,data:r,connectNulls:!1,marker:{enabled:!0,radius:2.5},step:`left`})}return{options:this.baseOptions(`Elite rank evolution`,`Elite positions at completed IRACE race checkpoints.`,{allowDecimals:!1,title:{text:`Race checkpoint`}},{allowDecimals:!1,min:1,reversed:!0,title:{text:`Elite position`}},a,`<b>{series.name}</b><br/>Checkpoint: {point.x}<br/>Position: {point.y}`)}}static instanceSummaries(n){let e=new Map;for(let i of n){let a=e.get(i.instanceName)??{instanceName:i.instanceName,evaluations:0,bestCost:null,issues:0,slow:0},o=a.bestCost;i.state===`SUCCEEDED`&&typeof i.cost==`number`&&Number.isFinite(i.cost)&&(o===null||i.cost<o)&&(o=i.cost),e.set(i.instanceName,{instanceName:a.instanceName,evaluations:a.evaluations+1,bestCost:o,issues:a.issues+(i.state===`REJECTED`||i.state===`FAILED`?1:0),slow:a.slow+(i.slow?1:0)})}return[...e.values()].sort((i,a)=>a.issues-i.issues||i.instanceName.localeCompare(a.instanceName))}static baseOptions(n,e,i,a,o,r){return{accessibility:{enabled:!0,description:e},boost:{enabled:!0,useGPUTranslations:!0,seriesThreshold:20},chart:{animation:!1,height:330,spacing:[12,12,12,12]},credits:{enabled:!1},exporting:{enabled:!0},legend:{enabled:!0,maxHeight:72},title:{text:n,style:{display:`none`}},xAxis:i,yAxis:a,tooltip:{headerFormat:``,pointFormat:r},plotOptions:{series:{animation:!1,connectNulls:!1}},series:o}}};var CP=500;var SP=2e3;var wP=1e4;var EP=1e4;var Pb=`mork.autoconfig.alertSeen.v1`;var Ba=class t{client=f(wm);destroyRef=f(Ne);statusSignal=M(null);componentUsageSignal=M(null);componentScopeSignal=M(`ALL`);componentErrorSignal=M(null);syncStateSignal=M(`detecting`);latestErrorSignal=M(null);evaluationsSignal=M([]);elitesSignal=M(null);eliteHistorySignal=M(null);searchSpaceSignal=M(null);artifactsSignal=M(null);alertsSignal=M([]);seenAlertRevisionSignal=M(0);runFailureSeenSignal=M(!1);lastUpdatedSignal=M(null);evaluationMap=new Map;candidateCache=new Map;pollTimer=null;currentRunId=null;evaluationRevision=0;eliteMarker=null;componentMarker=null;componentRequest=0;searchSpaceLoaded=!1;lastArtifactFetch=0;lastArtifactPhase=null;started=!1;destroyed=!1;status=this.statusSignal.asReadonly();componentUsage=this.componentUsageSignal.asReadonly();componentScope=this.componentScopeSignal.asReadonly();componentError=this.componentErrorSignal.asReadonly();syncState=this.syncStateSignal.asReadonly();latestError=this.latestErrorSignal.asReadonly();evaluations=this.evaluationsSignal.asReadonly();elites=this.elitesSignal.asReadonly();eliteHistory=this.eliteHistorySignal.asReadonly();searchSpace=this.searchSpaceSignal.asReadonly();artifacts=this.artifactsSignal.asReadonly();alerts=this.alertsSignal.asReadonly();lastUpdated=this.lastUpdatedSignal.asReadonly();viewMode=ne(()=>{let n=this.statusSignal();return n?n.mode===`DISABLED`?`standard`:n.role===`WORKER`?`worker`:`tuning`:this.syncStateSignal()===`retrying`?`unavailable`:`detecting`});connectionLabel=ne(()=>{switch(this.syncStateSignal()){case`detecting`:return`Detecting run mode`;case`synchronizing`:return`Synchronizing`;case`live`:return`Live`;case`retrying`:return`Reconnecting`}});budgetPercent=ne(()=>{let n=this.statusSignal()?.budget;return!n||n.maximum<=0?0:Math.min(100,n.used/n.maximum*100)});evaluationCostChart=ne(()=>jo.evaluationCost(this.evaluationsSignal()));evaluationActivityChart=ne(()=>jo.evaluationActivity(this.evaluationsSignal()));eliteRankChart=ne(()=>jo.eliteRanks(this.eliteHistorySignal()));instanceSummaries=ne(()=>jo.instanceSummaries(this.evaluationsSignal()));recentEvaluations=ne(()=>this.evaluationsSignal().slice(-100).reverse());unseenAlertCount=ne(()=>{let n=0;for(let e of this.alertsSignal())e.revision===null?this.runFailureSeenSignal()||n++:e.revision>this.seenAlertRevisionSignal()&&n++;return n});constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.clearPollTimer()})}start(){this.started||(this.started=!0,this.schedulePoll(0))}retryNow(){this.clearPollTimer(),this.schedulePoll(0)}markAlertsSeen(){this.seenAlertRevisionSignal.set(this.evaluationRevision),this.statusSignal()?.failure&&this.runFailureSeenSignal.set(!0),this.persistSeenAlerts()}async loadCandidate(n){let e=this.candidateCache.get(n);if(e)return e;let i=this.currentRunId,a=await this.client.getCandidate(n);if(i!==this.currentRunId)throw new Error(`Candidate belongs to an obsolete autoconfig run`);return(this.statusSignal()?.state===`COMPLETED`||this.statusSignal()?.state===`FAILED`)&&this.candidateCache.set(n,a),a}selectComponentScope(n){if(n===this.componentScopeSignal())return;this.componentScopeSignal.set(n),this.componentUsageSignal.set(null),this.componentMarker=null;let e=this.statusSignal();e&&this.synchronizeComponentUsage(e)}loadComponentCandidates(n){return this.client.getComponentCandidates(n)}refreshComponentUsage(){this.componentMarker=null;let n=this.statusSignal();n&&this.synchronizeComponentUsage(n)}async refreshArtifacts(){let n=this.statusSignal();if(!n?.runId||n.role!==`COORDINATOR`){this.artifactsSignal.set(null);return}try{this.artifactsSignal.set(await this.client.getArtifacts()),this.lastArtifactFetch=Date.now()}catch(e){if(this.isNotFound(e)){this.artifactsSignal.set({runId:n.runId,artifacts:[]}),this.lastArtifactFetch=Date.now();return}throw e}}artifactUrl(n){return this.client.artifactUrl(n)}schedulePoll(n){this.destroyed||(this.pollTimer=setTimeout(()=>{this.poll()},n))}async poll(){this.pollTimer=null,this.syncStateSignal.set(`synchronizing`);try{let n=await this.client.getStatus();this.adoptStatus(n),n.mode!==`DISABLED`&&n.role===`COORDINATOR`&&n.runId&&await this.synchronizeRun(n),this.latestErrorSignal.set(null),this.lastUpdatedSignal.set(new Date),this.syncStateSignal.set(`live`)}catch(n){this.latestErrorSignal.set(this.errorMessage(n)),this.syncStateSignal.set(`retrying`)}finally{let n=this.statusSignal(),e=n?.state===`PREPARING`||n?.state===`RUNNING`;this.schedulePoll(e?SP:wP)}}adoptStatus(n){n.runId!==this.currentRunId?this.resetRun(n.runId):n.latestEvaluationRevision<this.evaluationRevision&&this.resetRun(n.runId),this.statusSignal.set(n),this.publishAlerts()}async synchronizeRun(n){await this.synchronizeEvaluations(n),await this.synchronizeElites(n),await this.synchronizeSearchSpace(n),await this.synchronizeComponentUsage(n),await this.synchronizeArtifacts(n)}async synchronizeEvaluations(n){let e=!1;for(;this.evaluationRevision<n.latestEvaluationRevision;){let i=await this.client.getEvaluationChanges(this.evaluationRevision,CP);if(i.runId!==n.runId)throw new Error(`Evaluation changes belong to a different autoconfig run`);for(let a of i.changes)this.evaluationMap.set(a.evaluation.id,{revision:a.revision,evaluation:a.evaluation}),e=!0;if(i.nextRevision<=this.evaluationRevision)break;this.evaluationRevision=i.nextRevision}if(e){let i=[...this.evaluationMap.values()].sort((a,o)=>a.evaluation.id-o.evaluation.id).map(a=>a.evaluation);this.evaluationsSignal.set(i),this.publishAlerts()}}async synchronizeElites(n){let e=`${n.irace.iteration??``}|${n.irace.updatedAt??``}|${n.irace.finalSnapshot}`;if(e===this.eliteMarker)return;let[i,a]=await Promise.all([this.client.getElites(),this.client.getEliteHistory()]);if(i.runId!==n.runId||a.runId!==n.runId)throw new Error(`Elite snapshots belong to a different autoconfig run`);this.elitesSignal.set(i),this.eliteHistorySignal.set(a),this.eliteMarker=e}async synchronizeSearchSpace(n){if(!(n.mode!==`AUTOCONFIG`||n.generatedParameterCount<=0||this.searchSpaceLoaded))try{this.searchSpaceSignal.set(await this.client.getSearchSpace()),this.searchSpaceLoaded=!0}catch(e){if(!this.isNotFound(e))throw e}}async synchronizeComponentUsage(n){if(n.mode!==`AUTOCONFIG`||n.role!==`COORDINATOR`||!n.runId)return;let e=this.componentScopeSignal(),i=`${n.runId}|${e}|${n.latestEvaluationRevision}|${n.irace.updatedAt??``}|${n.irace.finalSnapshot}`;if(i===this.componentMarker)return;let a=++this.componentRequest;try{let o=await this.client.getComponentUsage(e);if(this.destroyed||a!==this.componentRequest||e!==this.componentScopeSignal())return;if(o.runId!==this.currentRunId||o.scope!==e)throw new Error(`Component usage belongs to a different run or scope`);this.componentUsageSignal.set(o),this.componentMarker=i,this.componentErrorSignal.set(null)}catch(o){a===this.componentRequest&&!this.destroyed&&this.componentErrorSignal.set(this.errorMessage(o))}}async synchronizeArtifacts(n){let e=Date.now();n.phase===this.lastArtifactPhase&&e-this.lastArtifactFetch<EP||(await this.refreshArtifacts(),this.lastArtifactPhase=n.phase)}publishAlerts(){let n=[],e=this.statusSignal();e?.failure&&n.push({key:`run:${e.runId}:failure`,revision:null,severity:`critical`,title:`Tuning run failed`,message:e.failure.message??e.failure.type,timestamp:e.finishedAt,evaluationId:null});for(let i of this.evaluationMap.values()){let a=i.evaluation;if(a.state!==`FAILED`&&a.state!==`REJECTED`&&!a.slow)continue;let o=a.state===`FAILED`,r=a.state===`REJECTED`,s=o?`Evaluation failed`:r?`Evaluation rejected`:`Slow evaluation`,c=a.message??a.reasonCode??a.instanceName;n.push({key:`evaluation:${a.id}`,revision:i.revision,severity:o?`critical`:`warning`,title:s,message:a.slow&&r?`${c} \xB7 also slow`:c,timestamp:a.finishedAt,evaluationId:a.id})}n.sort((i,a)=>i.revision===null?-1:a.revision===null?1:a.revision-i.revision),this.alertsSignal.set(n)}resetRun(n){this.currentRunId=n,this.evaluationRevision=0,this.evaluationMap.clear(),this.candidateCache.clear(),this.componentRequest++,this.componentUsageSignal.set(null),this.componentScopeSignal.set(`ALL`),this.componentErrorSignal.set(null),this.componentMarker=null,this.evaluationsSignal.set([]),this.elitesSignal.set(null),this.eliteHistorySignal.set(null),this.searchSpaceSignal.set(null),this.artifactsSignal.set(null),this.alertsSignal.set([]),this.eliteMarker=null,this.searchSpaceLoaded=!1,this.lastArtifactFetch=0,this.lastArtifactPhase=null,this.restoreSeenAlerts(n)}restoreSeenAlerts(n){if(this.seenAlertRevisionSignal.set(0),this.runFailureSeenSignal.set(!1),!!n)try{let e=localStorage.getItem(Pb);if(!e)return;let i=JSON.parse(e);i.runId===n&&(this.seenAlertRevisionSignal.set(typeof i.revision==`number`?i.revision:0),this.runFailureSeenSignal.set(i.runFailureSeen===!0))}catch{localStorage.removeItem(Pb)}}persistSeenAlerts(){if(!this.currentRunId)return;let n={runId:this.currentRunId,revision:this.seenAlertRevisionSignal(),runFailureSeen:this.runFailureSeenSignal()};localStorage.setItem(Pb,JSON.stringify(n))}clearPollTimer(){this.pollTimer&&(clearTimeout(this.pollTimer),this.pollTimer=null)}isNotFound(n){return n instanceof wi&&n.status===404}errorMessage(n){return n instanceof wi?`Backend request failed (${n.status||`network error`})`:n instanceof Error?n.message:String(n)}static ɵfac=function(e){return new(e||t)};static ɵprov=he({token:t,factory:t.ɵfac,providedIn:`root`})};var DP=(t,n)=>n[0];function kP(t,n){if(t&1&&(l(0,`dt`),p(1),d(),l(2,`dd`),p(3),d()),t&2){let e=n.$implicit;h(),w(e[0]),h(2),w(e[1])}}function IP(t,n){t&1&&(l(0,`dd`),p(1,`No parameters were reported.`),d())}function MP(t,n){if(t&1&&(l(0,`h3`),p(1,`Decoded algorithm`),d(),l(2,`pre`),p(3),Q(4,`json`),d()),t&2){let e=_();h(3),w(Ce(4,1,e.algorithm))}}function NP(t,n){if(t&1&&(l(0,`p`,1),p(1),d()),t&2){let e=_();h(),w(e.decodeError)}}function TP(t,n){if(t&1&&(l(0,`section`,4)(1,`span`)(2,`strong`),p(3),d(),p(4,` succeeded`),d(),l(5,`span`)(6,`strong`),p(7),d(),p(8,` running`),d(),l(9,`span`)(10,`strong`),p(11),d(),p(12,` rejected`),d(),l(13,`span`)(14,`strong`),p(15),d(),p(16,` failed`),d(),l(17,`span`)(18,`strong`),p(19),d(),p(20,` slow`),d()(),l(21,`h3`),p(22,`Parameters`),d(),l(23,`dl`),ve(24,kP,4,2,null,null,DP,!1,IP,2,0,`dd`),d(),N(27,MP,5,3),N(28,NP,2,1,`p`,1)),t&2){let e=n,i=_();h(3),w(e.evaluations.succeeded),h(4),w(e.evaluations.running),h(4),w(e.evaluations.rejected),h(4),w(e.evaluations.failed),h(4),w(e.evaluations.slow),h(5),ye(i.parameterEntries(e)),h(3),T(e.algorithm?27:-1),h(),T(e.decodeError?28:-1)}}function AP(t,n){if(t&1&&(l(0,`p`,1),p(1),d()),t&2){let e=_();h(),w(e.error())}}function OP(t,n){t&1&&(l(0,`p`),p(1,`Loading configuration…`),d())}var ls=class t{store=f(Ba);configurationId=f(Qc);candidate=M(null);error=M(null);ngOnInit(){this.store.loadCandidate(this.configurationId).then(n=>this.candidate.set(n)).catch(n=>this.error.set(n instanceof Error?n.message:String(n)))}parameterEntries(n){return Object.entries(n.parameters)}static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-candidate-dialog`]],decls:9,vars:2,consts:[[`mat-dialog-title`,``],[`role`,`alert`,1,`error`],[`align`,`end`],[`mat-button`,``,`mat-dialog-close`,``,`type`,`button`],[`aria-label`,`Candidate evaluation counts`,1,`counts`]],template:function(e,i){if(e&1&&(l(0,`h2`,0),p(1),d(),l(2,`mat-dialog-content`),N(3,TP,29,8)(4,AP,2,1,`p`,1)(5,OP,2,0,`p`),d(),l(6,`mat-dialog-actions`,2)(7,`button`,3),p(8,`Close`),d()()),e&2){let a;h(),H(`Configuration `,i.configurationId),h(2),T((a=i.candidate())?3:i.error()?4:5,a)}},dependencies:[Tt,Zt,Sm,ym,_m,Cm,xm,xo],styles:[`mat-dialog-content[_ngcontent-%COMP%]{min-width:min(42rem,80vw)}.counts[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:.5rem 1.25rem;margin-bottom:1.25rem}dl[_ngcontent-%COMP%]{display:grid;grid-template-columns:minmax(10rem,1fr) minmax(8rem,1fr);gap:.35rem 1rem}dt[_ngcontent-%COMP%]{overflow-wrap:anywhere;color:var(--%NS%mat-sys-on-surface-variant)}dd[_ngcontent-%COMP%]{margin:0;overflow-wrap:anywhere}pre[_ngcontent-%COMP%]{max-height:22rem;overflow:auto;padding:1rem;border-radius:.5rem;background:var(--%NS%mat-sys-surface-container);white-space:pre-wrap}.error[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-error)}`]})};var RP=()=>[25,50,100];var PP=(t,n)=>n.id;function FP(t,n){t&1&&(l(0,`span`,17),p(1,`slow`),d())}function LP(t,n){if(t&1){let e=ae();l(0,`tr`)(1,`td`),p(2),d(),l(3,`td`)(4,`button`,14),C(`click`,function(){let a=A(e).$implicit;return O(_().candidateSelected.emit(a.configurationId))}),p(5),d()(),l(6,`td`,15),p(7),d(),l(8,`td`)(9,`span`,16),p(10),d()(),l(11,`td`),p(12),Q(13,`number`),d(),l(14,`td`),p(15),Q(16,`number`),N(17,FP,2,0,`span`,17),d(),l(18,`td`),p(19),Q(20,`date`),d(),l(21,`td`,18),p(22),d()()}if(t&2){let e=n.$implicit,i=_();W(`issue`,e.state===`FAILED`||e.state===`REJECTED`),h(2),w(e.id),h(3),H(` `,e.configurationId,` `),h(),S(`title`,e.instanceName),h(),w(e.instanceName),h(2),et(i.stateClass(e.state)),h(),w(e.state),h(2),w(e.cost===null?`—`:He(13,14,e.cost,`1.0-5`)),h(3),H(` `,e.timeSeconds===null?`—`:He(16,17,e.timeSeconds,`1.2-2`)+` s`,` `),h(2),T(e.slow?17:-1),h(2),H(` `,e.finishedAt?He(20,20,e.finishedAt,`HH:mm:ss`):`Running`,` `),h(3),w(e.message??e.reasonCode??`—`)}}function VP(t,n){t&1&&(l(0,`tr`)(1,`td`,19),p(2,`No evaluations match these filters.`),d()())}var Em=class t{evaluations=jt.required();forceIssues=jt(!1);candidateSelected=Qd();search=M(``);state=M(`ALL`);slowOnly=M(!1);issueOnly=M(!1);pageIndex=M(0);pageSize=M(25);sortColumn=M(`id`);sortAscending=M(!1);filtered=ne(()=>{let n=this.search().trim().toLowerCase(),e=this.state(),i=this.slowOnly(),a=this.issueOnly()||this.forceIssues(),o=[];for(let c of this.evaluations())e!==`ALL`&&c.state!==e||i&&!c.slow||a&&c.state!==`FAILED`&&c.state!==`REJECTED`&&!c.slow||n&&!c.configurationId.toLowerCase().includes(n)&&!c.instanceName.toLowerCase().includes(n)&&!String(c.id).includes(n)||o.push(c);let r=this.sortColumn(),s=this.sortAscending()?1:-1;return o.sort((c,m)=>this.compare(c[r],m[r])*s),o});page=ne(()=>{let n=this.pageIndex()*this.pageSize();return this.filtered().slice(n,n+this.pageSize())});updateSearch(n){this.search.set(n),this.pageIndex.set(0)}updateState(n){this.state.set(n),this.pageIndex.set(0)}updateSlowOnly(n){this.slowOnly.set(n),this.pageIndex.set(0)}updateIssueOnly(n){this.issueOnly.set(n),this.pageIndex.set(0)}updatePage(n){this.pageIndex.set(n.pageIndex),this.pageSize.set(n.pageSize)}sortBy(n){this.sortColumn()===n?this.sortAscending.update(e=>!e):(this.sortColumn.set(n),this.sortAscending.set(!0)),this.pageIndex.set(0)}sortIndicator(n){return this.sortColumn()!==n?``:this.sortAscending()?` ↑`:` ↓`}stateClass(n){return`state-${n.toLowerCase()}`}compare(n,e){return n===e?0:n===null?1:e===null?-1:typeof n==`number`&&typeof e==`number`?n-e:String(n).localeCompare(String(e),void 0,{numeric:!0})}static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-evaluation-table`]],inputs:{evaluations:[1,`evaluations`],forceIssues:[1,`forceIssues`]},outputs:{candidateSelected:`candidateSelected`},decls:54,vars:17,consts:[[`aria-label`,`Evaluation filters`,1,`filters`],[`appearance`,`outline`,`subscriptSizing`,`dynamic`],[`matInput`,``,`type`,`search`,`placeholder`,`ID, configuration, or instance`,3,`ngModelChange`,`ngModel`],[3,`ngModelChange`,`ngModel`],[`value`,`ALL`],[`value`,`RUNNING`],[`value`,`SUCCEEDED`],[`value`,`REJECTED`],[`value`,`FAILED`],[3,`ngModelChange`,`ngModel`,`disabled`],[1,`table-scroll`],[`type`,`button`,3,`click`],[3,`issue`],[`aria-label`,`Evaluation pages`,3,`page`,`length`,`pageIndex`,`pageSize`,`pageSizeOptions`],[`type`,`button`,1,`candidate-link`,3,`click`],[1,`instance`,3,`title`],[1,`state`],[1,`slow`],[1,`details`],[`colspan`,`8`,1,`empty`]],template:function(e,i){e&1&&(l(0,`section`,0)(1,`mat-form-field`,1)(2,`mat-label`),p(3,`Search`),d(),l(4,`input`,2),Cr(),C(`ngModelChange`,function(o){return i.updateSearch(o)}),d()(),l(5,`mat-form-field`,1)(6,`mat-label`),p(7,`State`),d(),l(8,`mat-select`,3),Cr(),C(`ngModelChange`,function(o){return i.updateState(o)}),l(9,`mat-option`,4),p(10,`All states`),d(),l(11,`mat-option`,5),p(12,`Running`),d(),l(13,`mat-option`,6),p(14,`Succeeded`),d(),l(15,`mat-option`,7),p(16,`Rejected`),d(),l(17,`mat-option`,8),p(18,`Failed`),d()()(),l(19,`mat-checkbox`,3),Cr(),C(`ngModelChange`,function(o){return i.updateSlowOnly(o)}),p(20,` Slow only `),d(),l(21,`mat-checkbox`,9),Cr(),C(`ngModelChange`,function(o){return i.updateIssueOnly(o)}),p(22,` Issues only `),d()(),l(23,`div`,10)(24,`table`)(25,`thead`)(26,`tr`)(27,`th`)(28,`button`,11),C(`click`,function(){return i.sortBy(`id`)}),p(29),d()(),l(30,`th`)(31,`button`,11),C(`click`,function(){return i.sortBy(`configurationId`)}),p(32),d()(),l(33,`th`)(34,`button`,11),C(`click`,function(){return i.sortBy(`instanceName`)}),p(35),d()(),l(36,`th`)(37,`button`,11),C(`click`,function(){return i.sortBy(`state`)}),p(38),d()(),l(39,`th`)(40,`button`,11),C(`click`,function(){return i.sortBy(`cost`)}),p(41),d()(),l(42,`th`)(43,`button`,11),C(`click`,function(){return i.sortBy(`timeSeconds`)}),p(44),d()(),l(45,`th`),p(46,`Finished`),d(),l(47,`th`),p(48,`Details`),d()()(),l(49,`tbody`),ve(50,LP,23,23,`tr`,12,PP,!1,VP,3,0,`tr`),d()()(),l(53,`mat-paginator`,13),C(`page`,function(o){return i.updatePage(o)}),d()),e&2&&(h(4),S(`ngModel`,i.search()),Sr(),h(4),S(`ngModel`,i.state()),Sr(),h(11),S(`ngModel`,i.slowOnly()),Sr(),h(2),S(`ngModel`,i.issueOnly()||i.forceIssues())(`disabled`,i.forceIssues()),Sr(),h(8),H(`ID`,i.sortIndicator(`id`)),h(3),H(` Configuration`,i.sortIndicator(`configurationId`),` `),h(3),H(` Instance`,i.sortIndicator(`instanceName`),` `),h(3),H(`State`,i.sortIndicator(`state`)),h(3),H(`Cost`,i.sortIndicator(`cost`)),h(3),H(` Time`,i.sortIndicator(`timeSeconds`),` `),h(6),ye(i.page()),h(3),S(`length`,i.filtered().length)(`pageIndex`,i.pageIndex())(`pageSize`,i.pageSize())(`pageSizeOptions`,Bt(16,RP)))},dependencies:[TS,Uu,MS,eb,Tt,Xu,Vc,di,Mi,ta,Yr,qr,cm,$c,Ra,Oa,Hn,Qi,Sn],styles:[`[_nghost-%COMP%]{display:block;min-width:0}.filters[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:.75rem 1rem;align-items:center;margin-bottom:1rem}.filters[_ngcontent-%COMP%]   mat-form-field[_ngcontent-%COMP%]:first-child{flex:1 1 18rem}.table-scroll[_ngcontent-%COMP%]{overflow:auto}table[_ngcontent-%COMP%]{width:100%;min-width:70rem;border-collapse:collapse;font:var(--%NS%mat-sys-body-small)}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{padding:.65rem .55rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);text-align:left;vertical-align:middle}th[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font-weight:650}th[_ngcontent-%COMP%]   button[_ngcontent-%COMP%], .candidate-link[_ngcontent-%COMP%]{padding:0;border:0;color:inherit;background:transparent;cursor:pointer;font-weight:inherit}.candidate-link[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-primary);text-decoration:underline;text-underline-offset:.15rem}.instance[_ngcontent-%COMP%], .details[_ngcontent-%COMP%]{max-width:18rem;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.state[_ngcontent-%COMP%]{display:inline-block;padding:.15rem .45rem;border-radius:999px;background:var(--%NS%mat-sys-surface-container-high);font:var(--%NS%mat-sys-label-small)}.state-succeeded[_ngcontent-%COMP%]{color:#08743a;background:#d9f5e4}.state-rejected[_ngcontent-%COMP%], .slow[_ngcontent-%COMP%]{color:#835100;background:#fff0c6}.state-failed[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-error-container);background:var(--%NS%mat-sys-error-container)}.slow[_ngcontent-%COMP%]{margin-left:.25rem;padding:.1rem .3rem;border-radius:.25rem;font:var(--%NS%mat-sys-label-small)}tr.issue[_ngcontent-%COMP%]{background:color-mix(in srgb,var(--%NS%mat-sys-error-container) 22%,transparent)}.empty[_ngcontent-%COMP%]{padding:2.5rem;color:var(--%NS%mat-sys-on-surface-variant);text-align:center}mat-paginator[_ngcontent-%COMP%]{background:transparent}`]})};var Yw=(t,n)=>n.name;function BP(t,n){if(t&1&&(l(0,`tr`)(1,`td`),p(2),d(),l(3,`td`),p(4),d(),l(5,`td`),p(6),d()()),t&2){let e=n.$implicit,i=_(2);h(2),w(e.name),h(2),w(e.kind),h(2),w(i.domain(e))}}function jP(t,n){if(t&1&&(l(0,`details`)(1,`summary`),p(2),d(),l(3,`table`)(4,`thead`)(5,`tr`)(6,`th`),p(7,`Name`),d(),l(8,`th`),p(9,`Kind`),d(),l(10,`th`),p(11,`Domain`),d()()(),l(12,`tbody`),ve(13,BP,7,3,`tr`,null,Yw),d()()()),t&2){let e=n.$implicit;h(2),Ue(``,e.name,` · `,e.parameters.length,` parameters`),h(11),ye(e.parameters)}}var Dm=class t{data=f(Qc);domain(n){return n.choices.length>0?n.choices.join(`, `):n.values.length>0?n.values.join(`, `):n.minimum!==null||n.maximum!==null?`${String(n.minimum)} \u2013 ${String(n.maximum)}`:`Provided by Mork`}static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-search-space-dialog`]],decls:30,vars:5,consts:[[`mat-dialog-title`,``],[1,`summary`],[1,`components`],[`align`,`end`],[`mat-button`,``,`mat-dialog-close`,``,`type`,`button`]],template:function(e,i){e&1&&(l(0,`h2`,0),p(1,`Generated search space`),d(),l(2,`mat-dialog-content`)(3,`section`,1)(4,`span`)(5,`strong`),p(6),d(),p(7,` roots`),d(),l(8,`span`)(9,`strong`),p(10),d(),p(11,` components`),d(),l(12,`span`)(13,`strong`),p(14),d(),p(15,` IRACE parameters`),d(),l(16,`span`)(17,`strong`),p(18),d(),p(19,` constraints`),d()(),l(20,`p`)(21,`strong`),p(22,`Root choices:`),d(),p(23),d(),l(24,`section`,2),ve(25,jP,15,2,`details`,null,Yw),d()(),l(27,`mat-dialog-actions`,3)(28,`button`,4),p(29,`Close`),d()()),e&2&&(h(6),w(i.data.summary.rootCount),h(4),w(i.data.summary.componentCount),h(4),w(i.data.summary.generatedIraceParameterCount),h(4),w(i.data.summary.generatedForbiddenConstraintCount),h(5),H(` `,i.data.roots.join(`, `)),h(2),ye(i.data.components))},dependencies:[Tt,Zt,Sm,ym,_m,Cm,xm],styles:[`mat-dialog-content[_ngcontent-%COMP%]{width:min(60rem,84vw)}.summary[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:.75rem 2rem;margin-bottom:1rem}.components[_ngcontent-%COMP%]{display:grid;gap:.5rem}details[_ngcontent-%COMP%]{padding:.75rem;border:1px solid var(--%NS%mat-sys-outline-variant);border-radius:.5rem}summary[_ngcontent-%COMP%]{cursor:pointer;font-weight:650}table[_ngcontent-%COMP%]{width:100%;margin-top:.75rem;border-collapse:collapse}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{padding:.45rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);text-align:left;overflow-wrap:anywhere}`]})};var Kw=new D(`MAT_BUTTON_TOGGLE_DEFAULT_OPTIONS`,{providedIn:`root`,factory:()=>({hideSingleSelectionIndicator:!1,hideMultipleSelectionIndicator:!1,disabledInteractive:!1})});var Qw=new D(`MatButtonToggleGroup`);var zP={provide:Ur,useExisting:zt(()=>Fb),multi:!0};var km=class{source;value;constructor(n,e){this.source=n,this.value=e}};var Fb=(()=>{class t{_changeDetector=f(Re);_dir=f(pt,{optional:!0});_multiple=!1;_disabled=!1;_disabledInteractive=!1;_selectionModel;_rawValue;_controlValueAccessorChangeFn=()=>{};_onTouched=()=>{};_buttonToggles;appearance;get name(){return this._name}set name(e){this._name=e,this._markButtonsForCheck()}_name=f(Le).getId(`mat-button-toggle-group-`);vertical=!1;get value(){let e=this._selectionModel?this._selectionModel.selected:[];return this.multiple?e.map(i=>i.value):e[0]?e[0].value:void 0}set value(e){this._setSelectionByValue(e),this.valueChange.emit(this.value)}valueChange=new J;get selected(){let e=this._selectionModel?this._selectionModel.selected:[];return this.multiple?e:e[0]||null}get multiple(){return this._multiple}set multiple(e){this._multiple=e,this._markButtonsForCheck()}get disabled(){return this._disabled}set disabled(e){this._disabled=e,this._markButtonsForCheck()}get disabledInteractive(){return this._disabledInteractive}set disabledInteractive(e){this._disabledInteractive=e,this._markButtonsForCheck()}get dir(){return this._dir&&this._dir.value===`rtl`?`rtl`:`ltr`}change=new J;get hideSingleSelectionIndicator(){return this._hideSingleSelectionIndicator}set hideSingleSelectionIndicator(e){this._hideSingleSelectionIndicator=e,this._markButtonsForCheck()}_hideSingleSelectionIndicator;get hideMultipleSelectionIndicator(){return this._hideMultipleSelectionIndicator}set hideMultipleSelectionIndicator(e){this._hideMultipleSelectionIndicator=e,this._markButtonsForCheck()}_hideMultipleSelectionIndicator;constructor(){let e=f(Kw,{optional:!0});this.appearance=e&&e.appearance?e.appearance:`standard`,this._hideSingleSelectionIndicator=e?.hideSingleSelectionIndicator??!1,this._hideMultipleSelectionIndicator=e?.hideMultipleSelectionIndicator??!1}ngOnInit(){this._selectionModel=new Lo(this.multiple,void 0,!1)}ngAfterContentInit(){this._selectionModel.select(...this._buttonToggles.filter(e=>e.checked)),this.multiple||this._initializeTabIndex()}writeValue(e){this.value=e,this._changeDetector.markForCheck()}registerOnChange(e){this._controlValueAccessorChangeFn=e}registerOnTouched(e){this._onTouched=e}setDisabledState(e){this.disabled=e}_keydown(e){if(this.multiple||this.disabled||vt(e))return;let a=e.target.id,o=this._buttonToggles.toArray().findIndex(s=>s.buttonId===a),r=null;switch(e.keyCode){case 32:case 13:r=this._buttonToggles.get(o)||null;break;case 38:r=this._getNextButton(o,-1);break;case 37:r=this._getNextButton(o,this.dir===`ltr`?-1:1);break;case 40:r=this._getNextButton(o,1);break;case 39:r=this._getNextButton(o,this.dir===`ltr`?1:-1);break;default:return}r&&(e.preventDefault(),r._onButtonClick(),r.focus())}_emitChangeEvent(e){let i=new km(e,this.value);this._rawValue=i.value,this._controlValueAccessorChangeFn(i.value),this.change.emit(i)}_syncButtonToggle(e,i,a=!1,o=!1){!this.multiple&&this.selected&&!e.checked&&(this.selected.checked=!1),this._selectionModel?i?this._selectionModel.select(e):this._selectionModel.deselect(e):o=!0,o?Promise.resolve().then(()=>this._updateModelValue(e,a)):this._updateModelValue(e,a)}_isSelected(e){return this._selectionModel&&this._selectionModel.isSelected(e)}_isPrechecked(e){return typeof this._rawValue>`u`?!1:this.multiple&&Array.isArray(this._rawValue)?this._rawValue.some(i=>e.value!=null&&i===e.value):e.value===this._rawValue}_initializeTabIndex(){if(this._buttonToggles.forEach(e=>{e.tabIndex=-1}),this.selected)this.selected.tabIndex=0;else for(let e=0;e<this._buttonToggles.length;e++){let i=this._buttonToggles.get(e);if(!i.disabled){i.tabIndex=0;break}}}_getNextButton(e,i){let a=this._buttonToggles;for(let o=1;o<=a.length;o++){let r=(e+i*o+a.length)%a.length,s=a.get(r);if(s&&!s.disabled)return s}return null}_setSelectionByValue(e){if(this._rawValue=e,!this._buttonToggles)return;let i=this._buttonToggles.toArray();if(this.multiple&&e?(this._clearSelection(),e.forEach(a=>this._selectValue(a,i))):(this._clearSelection(),this._selectValue(e,i)),!this.multiple&&i.every(a=>a.tabIndex===-1)){for(let a of i)if(!a.disabled){a.tabIndex=0;break}}}_clearSelection(){this._selectionModel.clear(),this._buttonToggles.forEach(e=>{e.checked=!1,this.multiple||(e.tabIndex=-1)})}_selectValue(e,i){for(let a of i)if(a.value===e){a.checked=!0,this._selectionModel.select(a),this.multiple||(a.tabIndex=0);break}}_updateModelValue(e,i){i&&this._emitChangeEvent(e),this.valueChange.emit(this.value)}_markButtonsForCheck(){this._buttonToggles?.forEach(e=>e._markForCheck())}static ɵfac=function(i){return new(i||t)};static ɵdir=q({type:t,selectors:[[`mat-button-toggle-group`]],contentQueries:function(i,a,o){if(i&1&&Vt(o,Im,5),i&2){let r;X(r=Z())&&(a._buttonToggles=r)}},hostAttrs:[1,`mat-button-toggle-group`],hostVars:6,hostBindings:function(i,a){i&1&&C(`keydown`,function(r){return a._keydown(r)}),i&2&&(ee(`role`,a.multiple?`group`:`radiogroup`)(`aria-disabled`,a.disabled),W(`mat-button-toggle-vertical`,a.vertical)(`mat-button-toggle-group-appearance-standard`,a.appearance===`standard`))},inputs:{appearance:`appearance`,name:`name`,vertical:[2,`vertical`,`vertical`,V],value:`value`,multiple:[2,`multiple`,`multiple`,V],disabled:[2,`disabled`,`disabled`,V],disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,V],hideSingleSelectionIndicator:[2,`hideSingleSelectionIndicator`,`hideSingleSelectionIndicator`,V],hideMultipleSelectionIndicator:[2,`hideMultipleSelectionIndicator`,`hideMultipleSelectionIndicator`,V]},outputs:{valueChange:`valueChange`,change:`change`},exportAs:[`matButtonToggleGroup`],features:[$e([zP,{provide:Qw,useExisting:t}])]})}return t})();var Im=(()=>{class t{_changeDetectorRef=f(Re);_elementRef=f(G);_focusMonitor=f(Xt);_idGenerator=f(Le);_animationDisabled=ze();_checked=!1;ariaLabel;ariaLabelledby=null;_buttonElement;buttonToggleGroup;get buttonId(){return`${this.id}-button`}id;name;value;get tabIndex(){return this._tabIndex()}set tabIndex(e){this._tabIndex.set(e)}_tabIndex;disableRipple=!1;get appearance(){return this.buttonToggleGroup?this.buttonToggleGroup.appearance:this._appearance}set appearance(e){this._appearance=e}_appearance;get checked(){return this.buttonToggleGroup?this.buttonToggleGroup._isSelected(this):this._checked}set checked(e){e!==this._checked&&(this._checked=e,this.buttonToggleGroup&&this.buttonToggleGroup._syncButtonToggle(this,this._checked),this._changeDetectorRef.markForCheck())}get disabled(){return this._disabled||this.buttonToggleGroup&&this.buttonToggleGroup.disabled}set disabled(e){this._disabled=e}_disabled=!1;get disabledInteractive(){return this._disabledInteractive||this.buttonToggleGroup!==null&&this.buttonToggleGroup.disabledInteractive}set disabledInteractive(e){this._disabledInteractive=e}_disabledInteractive;change=new J;constructor(){f(Ze).load(cn);let e=f(Qw,{optional:!0}),i=f(new Yi(`tabindex`),{optional:!0})||``,a=f(Kw,{optional:!0});this._tabIndex=M(parseInt(i)||0),this.buttonToggleGroup=e,this._appearance=a&&a.appearance?a.appearance:`standard`,this._disabledInteractive=a?.disabledInteractive??!1}ngOnInit(){let e=this.buttonToggleGroup;this.id=this.id||this._idGenerator.getId(`mat-button-toggle-`),e&&(e._isPrechecked(this)?this.checked=!0:e._isSelected(this)!==this._checked&&e._syncButtonToggle(this,this._checked))}ngAfterViewInit(){this._animationDisabled||this._elementRef.nativeElement.classList.add(`mat-button-toggle-animations-enabled`),this._focusMonitor.monitor(this._elementRef,!0)}ngOnDestroy(){let e=this.buttonToggleGroup;this._focusMonitor.stopMonitoring(this._elementRef),e&&e._isSelected(this)&&e._syncButtonToggle(this,!1,!1,!0)}focus(e){this._buttonElement.nativeElement.focus(e)}_onButtonClick(){if(this.disabled)return;let e=this.isSingleSelector()?!0:!this._checked;if(e!==this._checked&&(this._checked=e,this.buttonToggleGroup&&(this.buttonToggleGroup._syncButtonToggle(this,this._checked,!0),this.buttonToggleGroup._onTouched())),this.isSingleSelector()){let i=this.buttonToggleGroup._buttonToggles.find(a=>a.tabIndex===0);i&&(i.tabIndex=-1),this.tabIndex=0}this.change.emit(new km(this,this.value))}_markForCheck(){this._changeDetectorRef.markForCheck()}_getButtonName(){return this.isSingleSelector()?this.buttonToggleGroup.name:this.name||null}isSingleSelector(){return this.buttonToggleGroup&&!this.buttonToggleGroup.multiple}static ɵfac=function(i){return new(i||t)};static ɵcmp=(function(){let e=[`button`],i=[`*`];function a(o,r){if(o&1&&(l(0,`div`,2),z(1,`mat-pseudo-checkbox`,6),d()),o&2){let s=_();h(),S(`disabled`,s.disabled)}}return B({type:t,selectors:[[`mat-button-toggle`]],viewQuery:function(r,s){if(r&1&&Xe(e,5),r&2){let c;X(c=Z())&&(s._buttonElement=c.first)}},hostAttrs:[`role`,`presentation`,1,`mat-button-toggle`],hostVars:14,hostBindings:function(r,s){r&1&&C(`focus`,function(){return s.focus()}),r&2&&(ee(`aria-label`,null)(`aria-labelledby`,null)(`id`,s.id)(`name`,null),W(`mat-button-toggle-standalone`,!s.buttonToggleGroup)(`mat-button-toggle-checked`,s.checked)(`mat-button-toggle-disabled`,s.disabled)(`mat-button-toggle-disabled-interactive`,s.disabledInteractive)(`mat-button-toggle-appearance-standard`,s.appearance===`standard`))},inputs:{ariaLabel:[0,`aria-label`,`ariaLabel`],ariaLabelledby:[0,`aria-labelledby`,`ariaLabelledby`],id:`id`,name:`name`,value:`value`,tabIndex:`tabIndex`,disableRipple:[2,`disableRipple`,`disableRipple`,V],appearance:`appearance`,checked:[2,`checked`,`checked`,V],disabled:[2,`disabled`,`disabled`,V],disabledInteractive:[2,`disabledInteractive`,`disabledInteractive`,V]},outputs:{change:`change`},exportAs:[`matButtonToggle`],ngContentSelectors:i,decls:7,vars:13,consts:[[`button`,``],[`type`,`button`,1,`mat-button-toggle-button`,`mat-focus-indicator`,3,`click`,`id`,`disabled`],[1,`mat-button-toggle-checkbox-wrapper`],[1,`mat-button-toggle-label-content`],[1,`mat-button-toggle-focus-overlay`],[`matRipple`,``,1,`mat-button-toggle-ripple`,3,`matRippleTrigger`,`matRippleDisabled`],[`state`,`checked`,`aria-hidden`,`true`,`appearance`,`minimal`,3,`disabled`]],template:function(r,s){if(r&1&&(Oe(),l(0,`button`,1,0),C(`click`,function(){return s._onButtonClick()}),N(2,a,2,1,`div`,2),l(3,`span`,3),ue(4),d()(),z(5,`span`,4)(6,`span`,5)),r&2){let c=mt(1);S(`id`,s.buttonId)(`disabled`,s.disabled&&!s.disabledInteractive||null),ee(`role`,s.isSingleSelector()?`radio`:`button`)(`tabindex`,s.disabled&&!s.disabledInteractive?-1:s.tabIndex)(`aria-pressed`,s.isSingleSelector()?null:s.checked)(`aria-checked`,s.isSingleSelector()?s.checked:null)(`name`,s._getButtonName())(`aria-label`,s.ariaLabel)(`aria-labelledby`,s.ariaLabelledby)(`aria-disabled`,s.disabled&&s.disabledInteractive?`true`:null),h(2),T(s.buttonToggleGroup&&(!s.buttonToggleGroup.multiple&&!s.buttonToggleGroup.hideSingleSelectionIndicator||s.buttonToggleGroup.multiple&&!s.buttonToggleGroup.hideMultipleSelectionIndicator)?2:-1),h(4),S(`matRippleTrigger`,c)(`matRippleDisabled`,s.disableRipple||s.disabled)}},dependencies:[ci,sm],styles:[`.mat-button-toggle-standalone,
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
`],encapsulation:2})})()}return t})();var Xw=(()=>{class t{static ɵfac=function(i){return new(i||t)};static ɵmod=ie({type:t});static ɵinj=te({imports:[ki,Im,we]})}return t})();var Un=class{static count(n,e){return e===`CANDIDATE`?n.candidatePlacements:n.evaluationPlacements}static components(n,e,i){let a=i.trim().toLocaleLowerCase();return(n?.components??[]).filter(o=>o.name.toLocaleLowerCase().includes(a)).sort((o,r)=>this.count(r,e)-this.count(o,e)||o.name.localeCompare(r.name))}static relationships(n,e,i,a){return(n?.relationships??[]).filter(o=>i===`incoming`?o.child===e:o.parent===e).sort((o,r)=>this.count(r,a)-this.count(o,a)||o.parent.localeCompare(r.parent)||o.role.localeCompare(r.role)||o.child.localeCompare(r.child))}static treemap(n,e,i){return{options:{chart:{type:`treemap`,height:390,animation:!1,backgroundColor:`transparent`},title:{text:void 0},credits:{enabled:!1},accessibility:{description:`Component placements. Select a tile or a component in the list to inspect its parents, children and configurations.`},tooltip:{pointFormat:`<b>{point.name}</b><br/>Placements: {point.value:,.0f}`},plotOptions:{series:{animation:!1}},series:[{type:`treemap`,name:`Component placements`,layoutAlgorithm:`squarified`,colorByPoint:!0,borderWidth:3,borderColor:`#fff`,colors:[`#2265a7`,`#00897b`,`#407fab`,`#49a69e`,`#697cad`,`#78b8ad`],dataLabels:{enabled:!0,format:`{point.name}<br/>{point.value:,.0f}`,style:{textOutline:`none`,fontWeight:`600`}},point:{events:{click:function(){this.name&&i(this.name)}}},data:n.filter(a=>this.count(a,e)>0).map(a=>({name:a.name,value:this.count(a,e)}))}]}}}static sankey(n,e,i,a,o){let r=[{id:`focus`,name:n??`Component`,color:`#2265a7`}],s=[],c=(u,g)=>{let v=u.filter(y=>this.count(y,a)>0);for(let[y,E]of v.slice(0,10).entries()){let R=`${g}:${y}`;r.push({id:R,name:g===`in`?`${E.parent} \xB7 ${E.role}`:`${E.role} \u2192 ${E.child}`,color:`#00897b`}),s.push({from:g===`in`?R:`focus`,to:g===`in`?`focus`:R,weight:this.count(E,a),custom:{edge:E}})}let b=v.slice(10).reduce((y,E)=>y+this.count(E,a),0);if(b>0){let y=`${g}:other`;r.push({id:y,name:`Other ${g===`in`?`parents`:`children`}`,color:`#aab7c5`}),s.push({from:g===`in`?y:`focus`,to:g===`in`?`focus`:y,weight:b})}};c(e,`in`),c(i,`out`);let m={type:`sankey`,animation:!1,name:`Direct relationships`,nodes:r,data:s,point:{events:{click:function(){let u=this.options.custom?.edge;u&&o(u)}}}};return{options:{chart:{height:Math.max(300,Math.min(560,Math.max(e.length,i.length)*32)),animation:!1,backgroundColor:`transparent`},title:{text:void 0},credits:{enabled:!1},accessibility:{description:`Direct parents and children of ${n??`the selected component`}. The complete role relationships are also listed in the inspector.`},series:[m]}}}static matrix(n,e,i,a){let o=new Set(this.components(n,e,i).map(y=>y.name)),r=i.trim().length>0,s=(n?.relationships??[]).filter(y=>!r||o.has(y.parent)||o.has(y.child)),c=new Set(o);for(let y of s)c.add(y.parent),c.add(y.child);let m=this.components(n,e,``).filter(y=>c.has(y.name)).sort((y,E)=>(r?Number(o.has(E.name))-Number(o.has(y.name)):0)||this.count(E,e)-this.count(y,e)||y.name.localeCompare(E.name)).slice(0,a),u=new Set(m.map(y=>y.name)),g=new Map,v=0;for(let y of s){if(!u.has(y.parent)||!u.has(y.child))continue;let E=JSON.stringify([y.parent,y.role]),R=g.get(E);R||(R={parent:y.parent,role:y.role,count:0,edges:new Map},g.set(E,R)),R.edges.set(y.child,y),R.count+=this.count(y,e),v=Math.max(v,this.count(y,e))}return{columns:m,rows:[...g.values()].sort((y,E)=>E.count-y.count||y.parent.localeCompare(E.parent)||y.role.localeCompare(E.role)).slice(0,a).map(y=>({parent:y.parent,role:y.role,cells:m.map(E=>y.edges.get(E.name)??null)})),maximum:v}}static parentPercent(n,e){let i=e?.components.find(a=>a.name===n.parent);return i?.configurationCount?100*n.configurationCount/i.configurationCount:0}};var UP=()=>[];var $P=(t,n)=>n.configurationId;var Zw=(t,n)=>n.name;var Jw=(t,n)=>n.parent+`:`+n.role;var GP=(t,n)=>n.role+`:`+n.child;function WP(t,n){t&1&&p(0,` Count each unique configuration tree once. Repeated components within a tree count as separate placements. `)}function qP(t,n){t&1&&p(0,` Weight each placement by accepted evaluations, including evaluations that later fail or are rejected. `)}function YP(t,n){if(t&1){let e=ae();l(0,`p`,13),p(1),l(2,`button`,15),C(`click`,function(){A(e);return O(_().store.refreshComponentUsage())}),p(3,`Retry`),d()()}t&2&&(h(),H(` `,n,` `))}function KP(t,n){if(t&1&&(l(0,`p`,17),p(1),Q(2,`number`),d()),t&2){let e=_();h(),H(` `,Ce(2,1,e.unavailableConfigurationCount),` configurations have unavailable component trees and are excluded from placement counts. `)}}function QP(t,n){if(t&1&&(l(0,`p`,14),p(1),d()),t&2){let e=_(2);h(),H(` `,e.store.componentScope()===`CURRENT_ELITES`?`Elites appear after the first race checkpoint.`:`Component usage appears when the first decoded candidate is evaluated.`,` `)}}function XP(t,n){t&1&&(l(0,`p`,14),p(1,`No components match this search.`),d())}function ZP(t,n){t&1&&(l(0,`p`,14),p(1,` These configurations have no accepted evaluations yet. Select candidate structures to inspect their composition. `),d())}function JP(t,n){if(t&1){let e=ae();l(0,`button`,31),C(`click`,function(){let a=A(e).$implicit;return O(_(4).selectComponent(a.name))}),l(1,`span`),p(2),d(),l(3,`strong`),p(4),Q(5,`number`),d()()}if(t&2){let e=n.$implicit,i=_(4);W(`selected`,i.selected()?.name===e.name),ee(`aria-pressed`,i.selected()?.name===e.name),h(2),w(e.name),h(2),w(Ce(5,5,i.count(e)))}}function eF(t,n){if(t&1&&(l(0,`mat-card-header`)(1,`mat-card-title`),p(2,`Component usage`),d()(),l(3,`mat-card-content`),z(4,`app-dashboard-chart`,28),N(5,ZP,2,0,`p`,14),l(6,`div`,29),ve(7,JP,6,7,`button`,30,Zw),d()()),t&2){let e=_(2),i=_();h(4),S(`model`,i.treemap()),h(),T(i.basis()===`EVALUATION`&&e.evaluationPlacementCount===0?5:-1),h(2),ye(i.components())}}function tF(t,n){if(t&1&&(l(0,`th`,25),p(1),d()),t&2){let e=n.$implicit;h(),w(e.name)}}function nF(t,n){if(t&1){let e=ae();l(0,`button`,31),C(`click`,function(){A(e);let a=_().$implicit;return O(_(5).selectRelationship(a))}),p(1),d()}if(t&2){let e=_().$implicit,i=_(5);mn(`background-color`,i.cellColor(e)),ee(`aria-label`,i.cellDescription(e)),h(),H(` `,i.cellLabel(e),` `)}}function iF(t,n){t&1&&(l(0,`span`),p(1,`—`),d())}function aF(t,n){if(t&1&&(l(0,`td`),N(1,nF,2,4,`button`,38)(2,iF,2,0,`span`),d()),t&2){let e=n.$implicit;h(),T(e?1:2)}}function oF(t,n){if(t&1&&(l(0,`tr`)(1,`th`,37),p(2),l(3,`small`),p(4),d()(),ve(5,aF,3,1,`td`,null,$f),d()),t&2){let e=n.$implicit;h(2),H(` `,e.parent),h(2),w(e.role),h(),ye(e.cells)}}function rF(t,n){if(t&1&&(l(0,`tr`)(1,`td`),p(2,` No direct relationships in this selection. `),d()()),t&2){let e=_(4);h(),ee(`colspan`,e.matrix().columns.length+1)}}function sF(t,n){if(t&1){let e=ae();l(0,`mat-card-header`)(1,`mat-card-title`),p(2,`Direct component relationships`),d()(),l(3,`mat-card-content`)(4,`div`,32)(5,`mat-form-field`,2)(6,`mat-label`),p(7,`Maximum rows / columns`),d(),l(8,`mat-select`,3),C(`selectionChange`,function(a){A(e);return O(_(3).matrixLimit.set(a.value))}),l(9,`mat-option`,33),p(10,`10`),d(),l(11,`mat-option`,33),p(12,`20`),d(),l(13,`mat-option`,33),p(14,`50`),d()()(),l(15,`mat-button-toggle-group`,34),C(`change`,function(a){A(e);return O(_(3).matrixPercent.set(a.value))}),l(16,`mat-button-toggle`,33),p(17,`Count`),d(),l(18,`mat-button-toggle`,33),p(19,`% of parent configurations`),d()()(),l(20,`p`,12),p(21,` Rows identify the parent and parameter role; columns identify the direct child. The most frequent matching components are shown. `),d(),l(22,`div`,35)(23,`table`,36)(24,`caption`),p(25,` Direct parent-role to child usage `),d(),l(26,`thead`)(27,`tr`)(28,`th`,25),p(29,`Parent · role`),d(),ve(30,tF,2,1,`th`,25,Zw),d()(),l(32,`tbody`),ve(33,oF,7,2,`tr`,null,Jw,!1,rF,3,1,`tr`),d()()()()}if(t&2){let e=_(3);h(8),S(`value`,e.matrixLimit()),h(),S(`value`,10),h(2),S(`value`,20),h(2),S(`value`,50),h(2),S(`value`,e.matrixPercent()),h(),S(`value`,!1),h(2),S(`value`,!0),h(12),ye(e.matrix().columns),h(3),ye(e.matrix().rows)}}function cF(t,n){if(t&1){let e=ae();l(0,`button`,31),C(`click`,function(){let a=A(e).$implicit;return O(_(4).selectRelationship(a))}),l(1,`span`)(2,`strong`),p(3),d(),l(4,`small`),p(5),d()(),l(6,`span`),p(7),Q(8,`number`),l(9,`small`),p(10),Q(11,`number`),d()()()}if(t&2){let e=n.$implicit,i=_(4);h(3),w(e.parent),h(2),w(e.role),h(2),w(Ce(8,4,i.count(e))),h(3),H(``,Ce(11,6,e.configurationCount),` configurations`)}}function lF(t,n){t&1&&(l(0,`p`),p(1,`No direct parents; this component is used only at the root.`),d())}function dF(t,n){if(t&1){let e=ae();l(0,`button`,31),C(`click`,function(){let a=A(e).$implicit;return O(_(4).selectRelationship(a))}),l(1,`span`)(2,`strong`),p(3),d(),l(4,`small`),p(5),d()(),l(6,`span`),p(7),Q(8,`number`),l(9,`small`),p(10),Q(11,`number`),d()()()}if(t&2){let e=n.$implicit,i=_(4);h(3),w(e.child),h(2),w(e.role),h(2),w(Ce(8,4,i.count(e))),h(3),H(``,Ce(11,6,e.configurationCount),` configurations`)}}function uF(t,n){t&1&&(l(0,`p`),p(1,`No direct child components.`),d())}function mF(t,n){if(t&1&&(l(0,`mat-card`,20)(1,`mat-card-header`)(2,`mat-card-title`),p(3),d(),l(4,`mat-card-subtitle`),p(5,`Component inspector`),d()(),l(6,`mat-card-content`)(7,`dl`)(8,`dt`),p(9,`Candidate placements`),d(),l(10,`dd`),p(11),Q(12,`number`),d(),l(13,`dt`),p(14,`Evaluation exposure`),d(),l(15,`dd`),p(16),Q(17,`number`),d(),l(18,`dt`),p(19,`Configuration coverage`),d(),l(20,`dd`),p(21),Q(22,`number`),Q(23,`number`),d(),l(24,`dt`),p(25,`Root placements / exposure`),d(),l(26,`dd`),p(27),Q(28,`number`),Q(29,`number`),d()(),l(30,`h3`),p(31,`Used by`),d(),l(32,`div`,39),ve(33,cF,12,8,`button`,40,Jw,!1,lF,2,0,`p`),d(),l(36,`h3`),p(37,`Contains`),d(),l(38,`div`,39),ve(39,dF,12,8,`button`,40,GP,!1,uF,2,0,`p`),d()()()),t&2){let e=n,i=_(3);h(3),w(e.name),h(8),w(Ce(12,9,e.candidatePlacements)),h(5),w(Ce(17,11,e.evaluationPlacements)),h(5),Ue(` `,Ce(22,13,e.configurationCount),` (`,He(23,15,i.selectedCoverage(),`1.0-1`),`%) `),h(6),Ue(` `,Ce(28,18,e.rootCandidatePlacements),` / `,Ce(29,20,e.rootEvaluationPlacements),` `),h(6),ye(i.incoming()),h(6),ye(i.outgoing())}}function pF(t,n){if(t&1&&(l(0,`mat-card`,21)(1,`mat-card-header`)(2,`mat-card-title`),p(3),d()(),l(4,`mat-card-content`),z(5,`app-dashboard-chart`,28),l(6,`p`,12),p(7,` Direct parents → selected component → direct children. Up to ten relationships are shown on each side; the inspector lists all relationships. `),d()()()),t&2){let e=_(3);h(3),H(`Neighborhood of `,e.selected()?.name),h(2),S(`model`,e.sankey())}}function hF(t,n){if(t&1){let e=ae();l(0,`p`),p(1),l(2,`button`,15),C(`click`,function(){A(e);return O(_(3).selectedEdge.set(null))}),p(3),d()()}if(t&2){let e=n,i=_(3);h(),Rn(` `,e.parent,` → `,e.role,` → `,e.child,` `),h(2),H(` Show all using `,i.selected()?.name,` `)}}function fF(t,n){if(t&1&&(l(0,`p`),p(1),d()),t&2){let e=_(3);h(),H(`Configurations containing `,e.selected()?.name)}}function gF(t,n){if(t&1){let e=ae();l(0,`p`,22),p(1),l(2,`button`,15),C(`click`,function(){A(e);return O(_(3).retryPage())}),p(3,`Retry`),d()()}t&2&&(h(),H(` `,n,` `))}function bF(t,n){if(t&1){let e=ae();l(0,`tr`)(1,`td`)(2,`button`,15),C(`click`,function(){let a=A(e).$implicit;return O(_(3).inspectCandidate(a.configurationId))}),p(3),d()(),l(4,`td`),p(5),Q(6,`number`),d(),l(7,`td`),p(8),Q(9,`number`),d(),l(10,`td`),p(11),d(),l(12,`td`),p(13),d()()}if(t&2){let e=n.$implicit;h(3),H(` `,e.configurationId,` `),h(2),w(Ce(6,8,e.occurrences)),h(3),w(Ce(9,10,e.evaluationPlacements)),h(3),qi(` `,e.evaluations.running,` / `,e.evaluations.succeeded,` / `,e.evaluations.rejected,` / `,e.evaluations.failed,` `),h(2),w(e.elitePosition??`—`)}}function vF(t,n){if(t&1&&(l(0,`tr`)(1,`td`,41),p(2),d()()),t&2){let e=_(3);h(2),H(` `,e.loading()?`Loading configurations…`:`No matching configurations.`,` `)}}function yF(t,n){if(t&1){let e=ae();l(0,`div`,18)(1,`mat-card`,19),N(2,eF,9,2)(3,sF,36,8),d(),N(4,mF,42,22,`mat-card`,20),N(5,pF,8,2,`mat-card`,21),l(6,`mat-card`,21)(7,`mat-card-header`)(8,`mat-card-title`),p(9,`Matching configurations`),d()(),l(10,`mat-card-content`),N(11,hF,4,4,`p`)(12,fF,2,1,`p`),N(13,gF,4,1,`p`,22),l(14,`div`,23)(15,`table`,24)(16,`thead`)(17,`tr`)(18,`th`,25),p(19,`Configuration`),d(),l(20,`th`,25),p(21,`Tree placements`),d(),l(22,`th`,25),p(23,`Evaluation exposure`),d(),l(24,`th`,25),p(25,`Running / succeeded / rejected / failed`),d(),l(26,`th`,25),p(27,`Elite rank`),d()()(),l(28,`tbody`),ve(29,bF,14,12,`tr`,null,$P,!1,vF,3,1,`tr`),d()()(),l(32,`div`,26)(33,`span`),p(34),Q(35,`number`),d(),l(36,`button`,27),C(`click`,function(){A(e);let a=_(2);return O(a.offset.set(a.offset()-25))}),p(37,` Previous `),d(),l(38,`span`),p(39),d(),l(40,`button`,27),C(`click`,function(){A(e);let a=_(2);return O(a.offset.set(a.page()?.nextOffset??0))}),p(41,` Next `),d()()()()()}if(t&2){let e,i,a,o=_(2);h(2),T(o.view()===`usage`?2:3),h(2),T((e=o.selected())?4:-1,e),h(),T(o.view()===`usage`&&(o.incoming().length>0||o.outgoing().length>0)?5:-1),h(6),T((i=o.relationship())?11:12,i),h(2),T((a=o.pageError())?13:-1,a),h(),ee(`aria-busy`,o.loading()),h(15),ye(o.page()?.candidates??Bt(13,UP)),h(5),H(``,Ce(35,11,o.page()?.total??0),` matching configurations`),h(2),S(`disabled`,o.offset()===0||o.loading()),h(3),H(`Page `,o.offset()/25+1),h(),S(`disabled`,o.page()?.nextOffset==null||o.loading())}}function _F(t,n){if(t&1&&(l(0,`div`,16)(1,`article`)(2,`span`),p(3,`Decoded configurations`),d(),l(4,`strong`),p(5),Q(6,`number`),Q(7,`number`),d()(),l(8,`article`)(9,`span`),p(10),d(),l(11,`strong`),p(12),Q(13,`number`),d()(),l(14,`article`)(15,`span`),p(16,`Distinct components`),d(),l(17,`strong`),p(18),Q(19,`number`),d()(),l(20,`article`)(21,`span`),p(22,`Accepted evaluations`),d(),l(23,`strong`),p(24),Q(25,`number`),d()()(),N(26,KP,3,3,`p`,17),N(27,QP,2,1,`p`,14)(28,XP,2,0,`p`,14)(29,yF,42,14,`div`,18)),t&2){let e=n,i=_();h(5),Ue(``,Ce(6,8,e.decodedConfigurationCount),` / `,Ce(7,10,e.configurationCount)),h(5),w(i.basis()===`CANDIDATE`?`Component placements`:`Evaluation placements`),h(2),w(Ce(13,12,i.basis()===`CANDIDATE`?e.candidatePlacementCount:e.evaluationPlacementCount)),h(6),w(Ce(19,14,e.components.length)),h(6),w(Ce(25,16,e.evaluationCount)),h(2),T(e.unavailableConfigurationCount>0?26:-1),h(),T(e.components.length===0?27:i.components().length===0?28:29)}}function xF(t,n){t&1&&(l(0,`p`,14),p(1,`Loading component usage…`),d())}var Mm=class t{store=f(Ba);dialog=f(Bo);destroyRef=f(Ne);basis=M(`CANDIDATE`);view=M(`usage`);search=M(``);matrixLimit=M(20);matrixPercent=M(!1);selectedName=M(null);selectedEdge=M(null);offset=M(0);page=M(null);loading=M(!1);pageError=M(null);pageRefresh=M(0);request=0;pageKey=null;destroyed=!1;components=ne(()=>Un.components(this.store.componentUsage(),this.basis(),this.search()));selected=ne(()=>this.components().find(n=>n.name===this.selectedName())??this.components()[0]);relationship=ne(()=>{let n=this.selectedEdge();return this.store.componentUsage()?.relationships.find(e=>n&&e.parent===n.parent&&e.role===n.role&&e.child===n.child)??null});incoming=ne(()=>Un.relationships(this.store.componentUsage(),this.selected()?.name,`incoming`,this.basis()));outgoing=ne(()=>Un.relationships(this.store.componentUsage(),this.selected()?.name,`outgoing`,this.basis()));treemap=ne(()=>Un.treemap(this.components(),this.basis(),n=>this.selectComponent(n)));sankey=ne(()=>Un.sankey(this.selected()?.name,this.incoming(),this.outgoing(),this.basis(),n=>this.selectRelationship(n)));matrix=ne(()=>Un.matrix(this.store.componentUsage(),this.basis(),this.search(),this.matrixLimit()));selector=ne(()=>{let n=this.relationship();if(n)return{parent:n.parent,role:n.role,child:n.child};let e=this.selected()?.name;return e?{component:e}:null});selectedCoverage=ne(()=>{let n=this.store.componentUsage()?.decodedConfigurationCount??0;return n?100*(this.selected()?.configurationCount??0)/n:0});selectionKey=ne(()=>JSON.stringify(this.selector()));constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.request++}),Pt(()=>{this.store.componentScope(),this.basis(),this.selectionKey(),this.store.status()?.runId,tt(()=>this.offset.set(0))}),Pt(()=>{let n=this.store.componentUsage(),e=this.selector(),i=this.store.componentScope(),a=this.basis(),o=this.offset();this.pageRefresh(),tt(()=>{if(!n||!e){this.request++,this.page.set(null),this.loading.set(!1),this.pageError.set(null);return}this.loadPage({scope:i,basis:a,selector:e,offset:o,limit:25})})})}selectComponent(n){this.selectedName.set(n),this.selectedEdge.set(null)}selectRelationship(n){this.selectedName.set(n.child),this.selectedEdge.set(n)}updateSearch(n){this.search.set(n.target.value),this.selectedEdge.set(null)}count(n){return Un.count(n,this.basis())}cellLabel(n){return this.matrixPercent()?`${Un.parentPercent(n,this.store.componentUsage()).toFixed(1)}%`:this.count(n).toLocaleString()}cellColor(n){let e=this.matrixPercent()?Un.parentPercent(n,this.store.componentUsage())/100:this.count(n)/(this.matrix().maximum||1);return`rgba(0, 137, 123, ${.08+Math.max(0,Math.min(1,e))*.65})`}cellDescription(n){return`${n.parent}, ${n.role}, ${n.child}: ${this.cellLabel(n)}`}inspectCandidate(n){this.dialog.open(ls,{data:n,width:`46rem`,maxWidth:`94vw`})}retryPage(){this.pageRefresh.update(n=>n+1)}async loadPage(n){let e=++this.request,i=this.store.componentUsage()?.runId;this.loading.set(!0),this.pageError.set(null);let a=JSON.stringify([i,n]);a!==this.pageKey&&this.page.set(null),this.pageKey=a;try{let o=await this.store.loadComponentCandidates(n);if(this.destroyed||e!==this.request)return;if(o.runId!==i||o.scope!==n.scope)throw new Error(`Configurations belong to a different run or scope`);if(n.offset>0&&o.total<=n.offset){this.offset.set(0);return}this.page.set(o)}catch(o){!this.destroyed&&e===this.request&&this.pageError.set(o instanceof Error?o.message:`Unable to load configurations`)}finally{!this.destroyed&&e===this.request&&this.loading.set(!1)}}static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-component-usage-dashboard`]],decls:33,vars:7,consts:[[1,`component-page`],[1,`controls`],[`appearance`,`outline`],[3,`selectionChange`,`value`],[`value`,`ALL`],[`value`,`CURRENT_ELITES`],[`value`,`CANDIDATE`],[`value`,`EVALUATION`],[`matInput`,``,3,`input`,`value`],[`aria-label`,`Component visualization`,3,`change`,`value`],[`value`,`usage`],[`value`,`matrix`],[1,`explanation`],[`role`,`alert`,1,`warning`],[1,`empty`],[`mat-button`,``,3,`click`],[1,`totals`],[1,`warning`],[1,`layout`],[`appearance`,`outlined`,1,`visualization`],[`appearance`,`outlined`,1,`inspector`],[`appearance`,`outlined`,1,`wide`],[`role`,`alert`],[1,`table-scroll`],[1,`candidate-table`],[`scope`,`col`],[1,`pagination`],[`mat-button`,``,3,`click`,`disabled`],[3,`model`],[`aria-label`,`Select a component`,1,`component-list`],[`type`,`button`,3,`selected`],[`type`,`button`,3,`click`],[1,`matrix-controls`],[3,`value`],[`aria-label`,`Matrix values`,3,`change`,`value`],[`tabindex`,`0`,`aria-label`,`Scrollable component relationship matrix`,1,`matrix-scroll`],[1,`matrix`],[`scope`,`row`],[`type`,`button`,3,`background-color`],[1,`relationships`],[`type`,`button`],[`colspan`,`5`]],template:function(e,i){if(e&1&&(l(0,`section`,0)(1,`div`,1)(2,`mat-form-field`,2)(3,`mat-label`),p(4,`Configurations`),d(),l(5,`mat-select`,3),C(`selectionChange`,function(o){return i.store.selectComponentScope(o.value)}),l(6,`mat-option`,4),p(7,`All evaluated candidates`),d(),l(8,`mat-option`,5),p(9,`Current elites`),d()()(),l(10,`mat-form-field`,2)(11,`mat-label`),p(12,`Count by`),d(),l(13,`mat-select`,3),C(`selectionChange`,function(o){return i.basis.set(o.value)}),l(14,`mat-option`,6),p(15,`Candidate structures`),d(),l(16,`mat-option`,7),p(17,`Evaluation exposure`),d()()(),l(18,`mat-form-field`,2)(19,`mat-label`),p(20,`Find component`),d(),l(21,`input`,8),C(`input`,function(o){return i.updateSearch(o)}),d()(),l(22,`mat-button-toggle-group`,9),C(`change`,function(o){return i.view.set(o.value)}),l(23,`mat-button-toggle`,10),p(24,`Usage map`),d(),l(25,`mat-button-toggle`,11),p(26,`Relationship matrix`),d()()(),l(27,`p`,12),N(28,WP,1,0)(29,qP,1,0),d(),N(30,YP,4,1,`p`,13),N(31,_F,30,18)(32,xF,2,0,`p`,14),d()),e&2){let a,o;h(5),S(`value`,i.store.componentScope()),h(8),S(`value`,i.basis()),h(8),S(`value`,i.search()),h(),S(`value`,i.view()),h(6),T(i.basis()===`CANDIDATE`?28:29),h(2),T((a=i.store.componentError())?30:-1,a),h(),T((o=i.store.componentUsage())?31:i.store.componentError()?-1:32,o)}},dependencies:[Tt,Zt,Xw,Fb,Im,jn,Vn,Bn,Ia,Au,ka,di,Mi,ta,Yr,qr,Ra,Oa,Hn,Ti,Sn],styles:[`[_nghost-%COMP%]{display:block}.component-page[_ngcontent-%COMP%]{padding:1.4rem 0}.controls[_ngcontent-%COMP%], .matrix-controls[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;gap:1rem;align-items:center}.controls[_ngcontent-%COMP%]   mat-form-field[_ngcontent-%COMP%]{flex:1 1 12rem}.explanation[_ngcontent-%COMP%]{color:#526477;line-height:1.5;font-size:.9rem;margin:.5rem 0 1.25rem}.totals[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(4,1fr);gap:1rem;margin-bottom:1.4rem}.totals[_ngcontent-%COMP%]   article[_ngcontent-%COMP%]{border:1px solid #d9e3ec;border-radius:.8rem;padding:1rem;background:#fff}.totals[_ngcontent-%COMP%]   span[_ngcontent-%COMP%], .totals[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{display:block}.totals[_ngcontent-%COMP%]   span[_ngcontent-%COMP%]{color:#526477;font-size:.85rem}.totals[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{color:#174b80;margin-top:.4rem;font-size:1.5rem}.warning[_ngcontent-%COMP%]{background:#fff5dc;padding:.8rem 1rem;border-radius:.6rem}.empty[_ngcontent-%COMP%]{padding:2rem;text-align:center;color:#526477}.layout[_ngcontent-%COMP%]{display:grid;grid-template-columns:minmax(0,2fr) minmax(18rem,1fr);gap:1.25rem}.layout[_ngcontent-%COMP%] > *[_ngcontent-%COMP%]{min-width:0}.wide[_ngcontent-%COMP%]{grid-column:1/-1}mat-card-header[_ngcontent-%COMP%]{margin-bottom:1rem}.component-list[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:.3rem;max-height:15rem;overflow:auto;margin-top:1rem}.component-list[_ngcontent-%COMP%]   button[_ngcontent-%COMP%], .relationships[_ngcontent-%COMP%]   button[_ngcontent-%COMP%]{border:0;background:#f3f7fb;cursor:pointer;border-radius:.4rem;padding:.65rem;display:flex;justify-content:space-between;gap:.5rem;text-align:left;color:#193b59}.component-list[_ngcontent-%COMP%]   button.selected[_ngcontent-%COMP%]{background:#d5e8f7;outline:2px solid #2265a7;outline-offset:-2px}.component-list[_ngcontent-%COMP%]   span[_ngcontent-%COMP%]{overflow-wrap:anywhere}.relationships[_ngcontent-%COMP%]{max-height:17rem;overflow:auto;display:grid;gap:.35rem}.relationships[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{display:block;color:#526477;margin-top:.25rem}.relationships[_ngcontent-%COMP%]   button[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:last-child{text-align:right}.relationships[_ngcontent-%COMP%]   p[_ngcontent-%COMP%]{color:#526477}dl[_ngcontent-%COMP%]{display:grid;grid-template-columns:1fr auto;gap:.8rem;font-size:.9rem}dt[_ngcontent-%COMP%]{color:#526477}dd[_ngcontent-%COMP%]{margin:0;text-align:right}h3[_ngcontent-%COMP%]{font-size:1rem;margin:1.4rem 0 .7rem}.matrix-scroll[_ngcontent-%COMP%], .table-scroll[_ngcontent-%COMP%]{overflow:auto}table[_ngcontent-%COMP%]{border-collapse:separate;border-spacing:0;font-size:.85rem;width:100%}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{padding:.65rem;border-bottom:1px solid #dce5ee;text-align:left}th[_ngcontent-%COMP%]{color:#526477;font-weight:600}.matrix[_ngcontent-%COMP%]   caption[_ngcontent-%COMP%]{text-align:left;padding:.5rem 0;color:#526477}.matrix[_ngcontent-%COMP%]   th[_ngcontent-%COMP%]{min-width:7rem}.matrix[_ngcontent-%COMP%]   th[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{display:block;margin-top:.2rem;font-weight:400}.matrix[_ngcontent-%COMP%]   tbody[_ngcontent-%COMP%]   th[_ngcontent-%COMP%]{position:sticky;left:0;background:#fff;z-index:1}.matrix[_ngcontent-%COMP%]   td[_ngcontent-%COMP%]{padding:.2rem;text-align:center}.matrix[_ngcontent-%COMP%]   td[_ngcontent-%COMP%]   button[_ngcontent-%COMP%]{width:100%;min-width:5rem;min-height:2.8rem;border:0;border-radius:.25rem;cursor:pointer;color:#112e3b}.pagination[_ngcontent-%COMP%]{display:flex;justify-content:flex-end;align-items:center;flex-wrap:wrap;gap:.7rem;margin-top:.8rem}.pagination[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:first-child{margin-right:auto}@media(max-width:1050px){.layout[_ngcontent-%COMP%]{grid-template-columns:1fr}.totals[_ngcontent-%COMP%]{grid-template-columns:repeat(2,1fr)}}@media(max-width:600px){.component-list[_ngcontent-%COMP%]{grid-template-columns:1fr}.controls[_ngcontent-%COMP%]   mat-button-toggle-group[_ngcontent-%COMP%], .matrix-controls[_ngcontent-%COMP%]   mat-button-toggle-group[_ngcontent-%COMP%]{max-width:100%;overflow:auto}.totals[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{font-size:1.2rem}}`]})};var Nm=()=>[];var CF=(t,n)=>n.key;var eE=(t,n)=>n.id;var tE=(t,n)=>n.configurationId;var SF=(t,n)=>n.instanceName;var wF=(t,n)=>n.iteration;function EF(t,n){if(t&1){let e=ae();l(0,`button`,19),C(`click`,function(){A(e);return O(_().showIssues())}),l(1,`span`,53)(2,`strong`),p(3),d(),l(4,`small`),p(5),d()()()}if(t&2){let e=n.$implicit;h(2),W(`critical`,e.severity===`critical`),h(),w(e.title),h(2),w(e.message)}}function DF(t,n){t&1&&(l(0,`span`,10),p(1,`No evaluation alerts`),d())}function kF(t,n){if(t&1){let e=ae();l(0,`button`,19),C(`click`,function(){A(e);return O(_().showIssues())}),l(1,`strong`),p(2,`View all issues`),d()()}}function IF(t,n){if(t&1&&(l(0,`section`,11)(1,`div`,54)(2,`span`,55),p(3,`Evaluation budget`),d(),l(4,`strong`),p(5),Q(6,`number`),Q(7,`number`),d(),l(8,`span`),p(9),Q(10,`number`),d()(),z(11,`mat-progress-bar`,56),l(12,`div`,57),z(13,`span`,58),p(14),d()()),t&2){let e=_(),i=Yf(0);h(5),Ue(``,Ce(6,7,i.budget.used),` / `,Ce(7,9,i.budget.maximum)),h(4),H(``,Ce(10,11,i.budget.remaining),` remaining`),h(2),S(`value`,e.store.budgetPercent()),h(2),W(`complete`,i.state===`COMPLETED`),h(),H(` `,e.phaseLabel(i.phase),` `)}}function MF(t,n){if(t&1){let e=ae();l(0,`section`,12)(1,`strong`),p(2,`Monitor connection problem`),d(),l(3,`span`),p(4),d(),l(5,`button`,50),C(`click`,function(){A(e);return O(_().store.retryNow())}),p(6,`Retry now`),d()()}t&2&&(h(4),w(n))}function NF(t,n){if(t&1&&(l(0,`section`,13)(1,`strong`),p(2),d(),l(3,`span`),p(4),d()()),t&2){let e=n;h(2),w(e.type),h(2),w(e.message??`The tuning run stopped unexpectedly.`)}}function TF(t,n){if(t&1){let e=ae();l(0,`button`,19),C(`click`,function(){A(e);let a=_().$implicit;return O(_().preferences.show(a.id))}),p(1),d()}if(t&2){let e=_().$implicit;h(),H(` Show `,e.label,` `)}}function AF(t,n){if(t&1&&N(0,TF,2,1,`button`,9),t&2){let e=n.$implicit;T(_().preferences.isHidden(e.id)?0:-1)}}function OF(t,n){if(t&1){let e=ae();l(0,`button`,59),C(`click`,function(){let a=A(e).$implicit;return O(_().inspectCandidate(a.configurationId))}),l(1,`span`,60),p(2),d(),l(3,`span`)(4,`strong`),p(5),d(),l(6,`small`),p(7),d()()()}if(t&2){let e=n.$implicit,i=_();h(),et(i.rankClass(e.position)),h(),w(e.position),h(3),H(`Configuration `,e.configurationId),h(2),w(i.parameterPreview(e))}}function RF(t,n){t&1&&(l(0,`p`,32),p(1,`Elites appear after the first completed race checkpoint.`),d())}function PF(t,n){if(t&1){let e=ae();l(0,`button`,61),C(`click`,function(){A(e);return O(_().inspectSearchSpace())}),p(1,` Inspect search space `),d()}}function FF(t,n){if(t&1&&(l(0,`tr`)(1,`td`),p(2),d(),l(3,`td`),p(4),d(),l(5,`td`),p(6),Q(7,`number`),d(),l(8,`td`),p(9),d(),l(10,`td`),p(11),d()()),t&2){let e=n.$implicit;h(2),w(e.instanceName),h(2),w(e.evaluations),h(2),H(` `,e.bestCost===null?`—`:He(7,5,e.bestCost,`1.0-5`),` `),h(3),w(e.issues),h(2),w(e.slow)}}function LF(t,n){t&1&&(l(0,`tr`)(1,`td`,62),p(2,`No instance activity yet.`),d()())}function VF(t,n){t&1&&p(0,` Final result `)}function BF(t,n){t&1&&p(0,` Latest completed checkpoint `)}function jF(t,n){if(t&1){let e=ae();l(0,`button`,59),C(`click`,function(){let a=A(e).$implicit;return O(_().inspectCandidate(a.configurationId))}),l(1,`span`,60),p(2),d(),l(3,`span`)(4,`strong`),p(5),d(),l(6,`small`),p(7),d()()()}if(t&2){let e=n.$implicit,i=_();h(),et(i.rankClass(e.position)),h(),w(e.position),h(3),H(`Configuration `,e.configurationId),h(2),w(i.parameterPreview(e))}}function zF(t,n){t&1&&(l(0,`p`,32),p(1,`No elite snapshot is available yet.`),d())}function HF(t,n){if(t&1&&(l(0,`article`)(1,`strong`),p(2),d(),l(3,`span`),p(4),Q(5,`date`),d(),l(6,`span`),p(7),Q(8,`number`),d(),l(9,`span`),p(10),d()()),t&2){let e=n.$implicit;h(2),H(`Checkpoint `,e.iteration),h(2),w(He(5,4,e.updatedAt,`medium`)),h(3),H(``,Ce(8,7,e.progress.experimentsUsed),` evaluations used`),h(3),H(``,e.elites.length,` elites`)}}function UF(t,n){t&1&&(l(0,`p`,32),p(1,`No completed race checkpoints yet.`),d())}function $F(t,n){t&1&&z(0,`app-component-usage-dashboard`)}function GF(t,n){t&1&&(l(0,`mat-tab`,48),at(1,$F,1,0,`ng-template`,63),d())}function WF(t,n){if(t&1&&(l(0,`a`,52)(1,`span`)(2,`strong`),p(3),d(),l(4,`small`),p(5),d()(),l(6,`span`),p(7),d(),l(8,`span`),p(9,`Download`),d()()),t&2){let e=n.$implicit,i=_();S(`href`,i.store.artifactUrl(e.id),Cn),h(3),w(e.filename),h(2),w(e.mediaType),h(2),w(i.artifactSize(e))}}function qF(t,n){t&1&&(l(0,`p`,32),p(1,` Artifacts appear as IRACE writes logs, plots, workspace data, and final elites. `),d())}var Tm=class t{store=f(Ba);preferences=f(La);panels=Ib;selectedTab=M(0);forceIssues=M(!1);dialog=f(Bo);constructor(){this.store.start()}selectTab(n){this.selectedTab.set(n.index),n.tab.textLabel!==`Evaluations`&&this.forceIssues.set(!1),n.tab.textLabel===`Artifacts`&&this.store.refreshArtifacts()}showIssues(){this.forceIssues.set(!0),this.selectedTab.set(1)}inspectCandidate(n){this.dialog.open(ls,{data:n,width:`46rem`,maxWidth:`94vw`})}inspectSearchSpace(){let n=this.store.searchSpace();n&&this.dialog.open(Dm,{data:n,maxWidth:`96vw`})}rankClass(n){return n<=3?`rank-${n}`:`rank-other`}phaseLabel(n){return n.toLowerCase().split(`_`).map(e=>e.charAt(0).toUpperCase()+e.slice(1)).join(` `)}elapsedLabel(n){if(n===null)return`—`;let e=Math.floor(n/1e3),i=Math.floor(e/3600),a=Math.floor(e%3600/60),o=e%60;return i>0?`${i}h ${a}m`:`${a}m ${o}s`}metricLabel(){let n=this.store.status()?.metric;if(!n)return`Waiting for metric details`;let e=n.kind===`AREA_UNDER_CURVE`?`Area under curve`:`Final objective`,i=n.negated?`maximized, converted to cost`:`minimized`;return`${n.objectiveName} \xB7 ${e} \xB7 ${i}`}parameterPreview(n){let e=Object.entries(n.parameters);return e.length===0?`No parameters`:e.slice(0,3).map(([i,a])=>`${i}=${a}`).join(` · `)}artifactSize(n){return n.size<1024?`${n.size} B`:n.size<1048576?`${(n.size/1024).toFixed(1)} KB`:`${(n.size/1048576).toFixed(1)} MB`}static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-tuning-dashboard`]],decls:183,vars:47,consts:[[`alertsMenu`,`matMenu`],[`panelMenu`,`matMenu`],[1,`topbar`],[1,`brand`],[`src`,`favicon.svg`,`alt`,``],[1,`toolbar-spacer`],[`aria-label`,`Synchronization state`],[`mat-button`,``,`type`,`button`,`matBadgeColor`,`warn`,`aria-label`,`Tuning alerts`,1,`alerts-button`,3,`menuOpened`,`matMenuTriggerFor`,`matBadge`,`matBadgeHidden`],[1,`alerts-menu`],[`mat-menu-item`,``,`type`,`button`],[1,`menu-empty`],[`aria-label`,`Evaluation budget`,1,`budget-ribbon`],[`role`,`alert`,1,`request-error`],[`role`,`alert`,1,`run-error`],[`animationDuration`,`150ms`,3,`selectedTabChange`,`selectedIndex`],[`label`,`Overview`],[1,`tab-page`,`overview`],[1,`overview-actions`],[`mat-button`,``,`type`,`button`,3,`matMenuTriggerFor`],[`mat-menu-item`,``,`type`,`button`,3,`click`],[`panelId`,`evaluation-health`,`title`,`Evaluation health`,1,`wide`],[1,`kpi-grid`],[1,`kpi`],[1,`kpi`,`success`],[1,`kpi`,`warning`],[1,`kpi`,`danger`],[`panelId`,`evaluation-cost`,`title`,`Evaluation cost`],[3,`model`],[`panelId`,`elite-evolution`,`title`,`Elite rank evolution`],[`panelId`,`current-elites`,`title`,`Current elites`],[1,`elite-list`],[`type`,`button`,1,`elite-row`],[1,`empty-state`],[`panelId`,`run-information`,`title`,`Run information`],[1,`run-info`],[`mat-stroked-button`,``,`type`,`button`],[`label`,`Evaluations`],[1,`tab-page`],[1,`two-column`],[`appearance`,`outlined`],[1,`summary-scroll`],[1,`summary-table`],[`appearance`,`outlined`,1,`evaluation-card`],[3,`candidateSelected`,`evaluations`,`forceIssues`],[`label`,`Elites`],[1,`two-column`,`elite-layout`],[1,`elite-list`,`detailed`],[1,`history-list`],[`label`,`Components`],[`label`,`Artifacts`],[`mat-button`,``,`type`,`button`,3,`click`],[1,`artifact-list`],[`download`,``,3,`href`],[1,`alert-item`],[1,`budget-copy`],[1,`eyebrow`],[`mode`,`determinate`,3,`value`],[1,`phase`],[1,`phase-dot`],[`type`,`button`,1,`elite-row`,3,`click`],[1,`medal`],[`mat-stroked-button`,``,`type`,`button`,3,`click`],[`colspan`,`5`,1,`empty-state`],[`matTabContent`,``]],template:function(e,i){if(e&1&&(Yd(0),l(1,`mat-toolbar`,2)(2,`span`,3),z(3,`img`,4),p(4,` Mork tuning monitor`),d(),z(5,`span`,5),l(6,`mat-chip-set`,6)(7,`mat-chip`),p(8),d()(),l(9,`button`,7),C(`menuOpened`,function(){return i.store.markAlertsSeen()}),p(10,` Alerts `),d(),l(11,`mat-menu`,8,0),ve(13,EF,6,4,`button`,9,CF,!1,DF,2,0,`span`,10),N(16,kF,3,0,`button`,9),d()(),N(17,IF,15,13,`section`,11),l(18,`main`),N(19,MF,7,1,`section`,12),N(20,NF,5,2,`section`,13),l(21,`mat-tab-group`,14),C(`selectedTabChange`,function(o){return i.selectTab(o)}),l(22,`mat-tab`,15)(23,`section`,16)(24,`div`,17)(25,`span`),p(26,`Live view of the current tuning run`),d(),l(27,`button`,18),p(28,`Panels`),d(),l(29,`mat-menu`,null,1),ve(31,AF,1,1,null,null,eE),l(33,`button`,19),C(`click`,function(){return i.preferences.reset()}),p(34,` Restore all panels `),d()()(),l(35,`app-monitor-panel`,20)(36,`section`,21)(37,`article`,22)(38,`span`),p(39,`Running`),d(),l(40,`strong`),p(41),d()(),l(42,`article`,23)(43,`span`),p(44,`Succeeded`),d(),l(45,`strong`),p(46),d()(),l(47,`article`,24)(48,`span`),p(49,`Rejected`),d(),l(50,`strong`),p(51),d()(),l(52,`article`,25)(53,`span`),p(54,`Failed`),d(),l(55,`strong`),p(56),d()(),l(57,`article`,24)(58,`span`),p(59,`Slow`),d(),l(60,`strong`),p(61),d()()()(),l(62,`app-monitor-panel`,26),z(63,`app-dashboard-chart`,27),d(),l(64,`app-monitor-panel`,28),z(65,`app-dashboard-chart`,27),d(),l(66,`app-monitor-panel`,29)(67,`div`,30),ve(68,OF,8,5,`button`,31,tE,!1,RF,2,0,`p`,32),d()(),l(71,`app-monitor-panel`,33)(72,`dl`,34)(73,`dt`),p(74,`Run ID`),d(),l(75,`dd`),p(76),d(),l(77,`dt`),p(78,`Mode`),d(),l(79,`dd`),p(80),d(),l(81,`dt`),p(82,`Elapsed`),d(),l(83,`dd`),p(84),d(),l(85,`dt`),p(86,`Training instances`),d(),l(87,`dd`),p(88),d(),l(89,`dt`),p(90,`Generated parameters`),d(),l(91,`dd`),p(92),d(),l(93,`dt`),p(94,`Cost metric`),d(),l(95,`dd`),p(96),d(),l(97,`dt`),p(98,`Last update`),d(),l(99,`dd`),p(100),Q(101,`date`),d()(),N(102,PF,2,0,`button`,35),d()()(),l(103,`mat-tab`,36)(104,`section`,37)(105,`section`,38)(106,`mat-card`,39)(107,`mat-card-header`)(108,`mat-card-title`),p(109,`Evaluation activity`),d()(),l(110,`mat-card-content`),z(111,`app-dashboard-chart`,27),d()(),l(112,`mat-card`,39)(113,`mat-card-header`)(114,`mat-card-title`),p(115,`Instance summary`),d()(),l(116,`mat-card-content`,40)(117,`table`,41)(118,`thead`)(119,`tr`)(120,`th`),p(121,`Instance`),d(),l(122,`th`),p(123,`Runs`),d(),l(124,`th`),p(125,`Best cost`),d(),l(126,`th`),p(127,`Issues`),d(),l(128,`th`),p(129,`Slow`),d()()(),l(130,`tbody`),ve(131,FF,12,8,`tr`,null,SF,!1,LF,3,0,`tr`),d()()()()(),l(134,`mat-card`,42)(135,`mat-card-header`)(136,`mat-card-title`),p(137,`Evaluation ledger`),d()(),l(138,`mat-card-content`)(139,`app-evaluation-table`,43),C(`candidateSelected`,function(o){return i.inspectCandidate(o)}),d()()()()(),l(140,`mat-tab`,44)(141,`section`,37)(142,`section`,45)(143,`mat-card`,39)(144,`mat-card-header`)(145,`mat-card-title`),p(146,`Current elite set`),d(),l(147,`mat-card-subtitle`),N(148,VF,1,0)(149,BF,1,0),d()(),l(150,`mat-card-content`)(151,`div`,46),ve(152,jF,8,5,`button`,31,tE,!1,zF,2,0,`p`,32),d()()(),l(155,`mat-card`,39)(156,`mat-card-header`)(157,`mat-card-title`),p(158,`Rank history`),d()(),l(159,`mat-card-content`),z(160,`app-dashboard-chart`,27),d()()(),l(161,`mat-card`,39)(162,`mat-card-header`)(163,`mat-card-title`),p(164,`Race checkpoints`),d()(),l(165,`mat-card-content`,47),ve(166,HF,11,9,`article`,null,wF,!1,UF,2,0,`p`,32),d()()()(),N(169,GF,2,0,`mat-tab`,48),l(170,`mat-tab`,49)(171,`section`,37)(172,`mat-card`,39)(173,`mat-card-header`)(174,`mat-card-title`),p(175,`Run artifacts`),d(),z(176,`span`,5),l(177,`button`,50),C(`click`,function(){return i.store.refreshArtifacts()}),p(178,`Refresh`),d()(),l(179,`mat-card-content`,51),ve(180,WF,10,4,`a`,52,eE,!1,qF,2,0,`p`,32),d()()()()()()),e&2){let a,o,r=mt(12),s=mt(30),c=qf(i.store.status());h(7),W(`live`,i.store.syncState()===`live`),h(),w(i.store.connectionLabel()),h(),S(`matMenuTriggerFor`,r)(`matBadge`,i.store.unseenAlertCount())(`matBadgeHidden`,i.store.unseenAlertCount()===0),h(4),ye(i.store.alerts().slice(0,10)),h(3),T(i.store.alerts().length>0?16:-1),h(),T(c?17:-1),h(2),T((a=i.store.latestError())?19:-1,a),h(),T((o=c?.failure)?20:-1,o),h(),S(`selectedIndex`,i.selectedTab()),h(6),S(`matMenuTriggerFor`,s),h(4),ye(i.panels),h(10),w(c?.evaluations?.running??0),h(5),w(c?.evaluations?.succeeded??0),h(5),w(c?.evaluations?.rejected??0),h(5),w(c?.evaluations?.failed??0),h(5),w(c?.evaluations?.slow??0),h(2),S(`model`,i.store.evaluationCostChart()),h(2),S(`model`,i.store.eliteRankChart()),h(3),ye(i.store.elites()?.elites??Bt(43,Nm)),h(8),w(c?.runId??`—`),h(4),w(c?.mode??`—`),h(4),w(i.elapsedLabel(c?.elapsedMillis??null)),h(4),w(c?.trainingInstanceCount??`Preparing…`),h(4),w(c?.generatedParameterCount??0),h(4),w(i.metricLabel()),h(4),w(He(101,40,i.store.lastUpdated(),`mediumTime`)),h(2),T(i.store.searchSpace()?102:-1),h(9),S(`model`,i.store.evaluationActivityChart()),h(20),ye(i.store.instanceSummaries()),h(8),S(`evaluations`,i.store.evaluations())(`forceIssues`,i.forceIssues()),h(9),T(i.store.elites()?.finalSnapshot?148:149),h(4),ye(i.store.elites()?.elites??Bt(44,Nm)),h(8),S(`model`,i.store.eliteRankChart()),h(6),ye(i.store.eliteHistory()?.iterations??Bt(45,Nm)),h(3),T(c?.mode===`AUTOCONFIG`?169:-1),h(11),ye(i.store.artifacts()?.artifacts??Bt(46,Nm))}},dependencies:[Ti,Em,Vw,Lw,Tt,Zt,jn,Vn,Bn,Ia,Au,ka,Qu,Lc,Ku,ns,Ni,sa,ts,as,is,dm,Gc,Wc,lm,mm,um,ss,Mm,Qi,Sn],styles:[`[_nghost-%COMP%]{display:block;min-height:100vh;color:var(--%NS%mat-sys-on-surface);background:radial-gradient(circle at 92% -5%,color-mix(in srgb,var(--%NS%mat-sys-tertiary) 14%,transparent),transparent 28rem),var(--%NS%mat-sys-surface-container-lowest)}.topbar[_ngcontent-%COMP%]{position:sticky;top:0;z-index:20;gap:.75rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);background:color-mix(in srgb,var(--%NS%mat-sys-surface) 94%,transparent);-webkit-backdrop-filter:blur(12px);backdrop-filter:blur(12px)}.brand[_ngcontent-%COMP%]{display:flex;gap:.65rem;align-items:center;font:var(--%NS%mat-sys-title-large);font-weight:750;letter-spacing:-.025em}.brand[_ngcontent-%COMP%]   img[_ngcontent-%COMP%]{width:2rem;height:2rem}.toolbar-spacer[_ngcontent-%COMP%]{flex:1}mat-chip.live[_ngcontent-%COMP%]{--%NS%mdc-chip-elevated-container-color: color-mix(in srgb, var(--%NS%mat-sys-tertiary) 20%, transparent)}.alerts-button[_ngcontent-%COMP%]{margin-right:.5rem}.alert-item[_ngcontent-%COMP%]{display:grid;width:min(24rem,72vw);line-height:1.25}.alert-item[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{overflow:hidden;color:var(--%NS%mat-sys-on-surface-variant);text-overflow:ellipsis}.critical[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-error)}.menu-empty[_ngcontent-%COMP%]{display:block;padding:1rem;color:var(--%NS%mat-sys-on-surface-variant)}.budget-ribbon[_ngcontent-%COMP%]{position:sticky;top:4rem;z-index:15;display:grid;grid-template-columns:minmax(15rem,auto) minmax(12rem,1fr) auto;gap:1.5rem;align-items:center;padding:.65rem max(1rem,(100% - 96rem) / 2);border-bottom:1px solid var(--%NS%mat-sys-outline-variant);background:color-mix(in srgb,var(--%NS%mat-sys-surface-container-low) 96%,transparent);-webkit-backdrop-filter:blur(12px);backdrop-filter:blur(12px)}.budget-copy[_ngcontent-%COMP%]{display:flex;gap:.55rem;align-items:baseline;white-space:nowrap}.budget-copy[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:last-child{color:var(--%NS%mat-sys-on-surface-variant);font:var(--%NS%mat-sys-body-small)}.eyebrow[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font:var(--%NS%mat-sys-label-small);text-transform:uppercase;letter-spacing:.07em}.phase[_ngcontent-%COMP%]{display:flex;gap:.4rem;align-items:center;font:var(--%NS%mat-sys-label-large);white-space:nowrap}.phase-dot[_ngcontent-%COMP%]{width:.6rem;height:.6rem;border-radius:50%;background:var(--%NS%mat-sys-tertiary);box-shadow:0 0 0 .2rem color-mix(in srgb,var(--%NS%mat-sys-tertiary) 18%,transparent)}.phase-dot.complete[_ngcontent-%COMP%]{background:#079447}main[_ngcontent-%COMP%]{width:min(100% - 2rem,96rem);margin:0 auto;padding-bottom:4rem}.request-error[_ngcontent-%COMP%], .run-error[_ngcontent-%COMP%]{display:flex;gap:.75rem;align-items:center;margin:1rem 0 0;padding:.75rem 1rem;border:1px solid var(--%NS%mat-sys-error);border-radius:.75rem;color:var(--%NS%mat-sys-on-error-container);background:var(--%NS%mat-sys-error-container)}.request-error[_ngcontent-%COMP%]   button[_ngcontent-%COMP%]{margin-left:auto}.tab-page[_ngcontent-%COMP%]{display:grid;gap:1rem;padding:1.25rem 0}.overview[_ngcontent-%COMP%]{grid-template-columns:repeat(2,minmax(0,1fr))}.wide[_ngcontent-%COMP%], .overview-actions[_ngcontent-%COMP%]{grid-column:1/-1}.overview-actions[_ngcontent-%COMP%]{display:flex;align-items:center;min-height:2rem;color:var(--%NS%mat-sys-on-surface-variant)}.overview-actions[_ngcontent-%COMP%]   button[_ngcontent-%COMP%]{margin-left:auto}.kpi-grid[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(5,minmax(7rem,1fr));gap:.75rem}.kpi[_ngcontent-%COMP%]{display:grid;gap:.2rem;padding:.8rem 1rem;border-left:.25rem solid var(--%NS%mat-sys-primary);border-radius:.4rem;background:var(--%NS%mat-sys-surface-container-low)}.kpi[_ngcontent-%COMP%]   span[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant);font:var(--%NS%mat-sys-label-medium)}.kpi[_ngcontent-%COMP%]   strong[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-headline-small)}.kpi.success[_ngcontent-%COMP%]{border-color:#079447}.kpi.warning[_ngcontent-%COMP%]{border-color:#ed8e00}.kpi.danger[_ngcontent-%COMP%]{border-color:var(--%NS%mat-sys-error)}.elite-list[_ngcontent-%COMP%]{display:grid;gap:.5rem}.elite-row[_ngcontent-%COMP%]{display:grid;grid-template-columns:2.3rem minmax(0,1fr);gap:.75rem;align-items:center;width:100%;padding:.65rem;border:1px solid transparent;border-radius:.5rem;color:inherit;background:transparent;text-align:left;cursor:pointer}.elite-row[_ngcontent-%COMP%]:hover{border-color:var(--%NS%mat-sys-outline-variant);background:var(--%NS%mat-sys-surface-container-low)}.elite-row[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:last-child{display:grid;min-width:0}.elite-row[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{overflow:hidden;color:var(--%NS%mat-sys-on-surface-variant);text-overflow:ellipsis;white-space:nowrap}.medal[_ngcontent-%COMP%]{display:grid;place-items:center;width:2rem;height:2rem;border-radius:50%;color:#292929;background:#d1d5db;font-weight:750}.rank-1[_ngcontent-%COMP%]{background:#f2c94c}.rank-2[_ngcontent-%COMP%]{background:#c7ccd4}.rank-3[_ngcontent-%COMP%]{background:#cd8b55}.rank-other[_ngcontent-%COMP%]{background:#e5e7eb}.run-info[_ngcontent-%COMP%]{display:grid;grid-template-columns:minmax(8rem,auto) minmax(0,1fr);gap:.55rem 1rem;margin:0 0 1rem}.run-info[_ngcontent-%COMP%]   dt[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant)}.run-info[_ngcontent-%COMP%]   dd[_ngcontent-%COMP%]{margin:0;overflow-wrap:anywhere}.two-column[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:1rem}mat-card-header[_ngcontent-%COMP%]{align-items:center;padding-bottom:.75rem}.summary-scroll[_ngcontent-%COMP%]{max-height:21rem;overflow:auto}.summary-table[_ngcontent-%COMP%]{width:100%;border-collapse:collapse}.summary-table[_ngcontent-%COMP%]   th[_ngcontent-%COMP%], .summary-table[_ngcontent-%COMP%]   td[_ngcontent-%COMP%]{padding:.55rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);text-align:left}.evaluation-card[_ngcontent-%COMP%]   mat-card-content[_ngcontent-%COMP%]{min-width:0}.history-list[_ngcontent-%COMP%]{display:grid;gap:.5rem}.history-list[_ngcontent-%COMP%]   article[_ngcontent-%COMP%]{display:grid;grid-template-columns:1fr repeat(3,auto);gap:1rem;padding:.75rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant)}.history-list[_ngcontent-%COMP%]   article[_ngcontent-%COMP%]   span[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant)}.artifact-list[_ngcontent-%COMP%]{display:grid}.artifact-list[_ngcontent-%COMP%]   a[_ngcontent-%COMP%]{display:grid;grid-template-columns:minmax(0,1fr) auto auto;gap:1.5rem;align-items:center;padding:1rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant);color:var(--%NS%mat-sys-primary);text-decoration:none}.artifact-list[_ngcontent-%COMP%]   a[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:first-child{display:grid}.artifact-list[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-on-surface-variant)}.empty-state[_ngcontent-%COMP%]{padding:1.5rem 0;color:var(--%NS%mat-sys-on-surface-variant);text-align:center}@media(max-width:62rem){.overview[_ngcontent-%COMP%], .two-column[_ngcontent-%COMP%]{grid-template-columns:1fr}.kpi-grid[_ngcontent-%COMP%]{grid-template-columns:repeat(3,1fr)}.budget-ribbon[_ngcontent-%COMP%]{grid-template-columns:1fr auto}.budget-ribbon[_ngcontent-%COMP%]   mat-progress-bar[_ngcontent-%COMP%]{grid-column:1/-1;grid-row:2}}@media(max-width:42rem){.topbar[_ngcontent-%COMP%]   mat-chip-set[_ngcontent-%COMP%]{display:none}.brand[_ngcontent-%COMP%]{font:var(--%NS%mat-sys-title-medium)}.budget-ribbon[_ngcontent-%COMP%]{top:3.5rem;gap:.5rem}.budget-copy[_ngcontent-%COMP%] > span[_ngcontent-%COMP%]:last-child, .eyebrow[_ngcontent-%COMP%]{display:none}main[_ngcontent-%COMP%]{width:min(100% - 1rem,96rem)}.kpi-grid[_ngcontent-%COMP%]{grid-template-columns:repeat(2,1fr)}.history-list[_ngcontent-%COMP%]   article[_ngcontent-%COMP%], .artifact-list[_ngcontent-%COMP%]   a[_ngcontent-%COMP%]{grid-template-columns:1fr;gap:.2rem}}`]})};var Am=class t{http=f(Ei);destroy=f(Ne);base=`${Dn.apiBaseUrl}/api/autollm`;get(n,e={}){return At(this.http.get(this.base+n,{params:e}).pipe(En(this.destroy)))}post(n,e={}){return At(this.http.post(this.base+n,e).pipe(En(this.destroy)))}defaults(){return this.get(`/defaults`)}validate(n){return this.post(`/validate`,n)}list(){return this.get(`/campaigns`)}create(n){return this.post(`/campaigns`,n)}action(n,e){return this.post(`/campaigns/${encodeURIComponent(n)}/${e}`)}progress(n,e){return this.get(`/campaigns/${encodeURIComponent(n)}/trials/${e}/progress`)}comparison(n,e){return this.get(`/campaigns/${encodeURIComponent(n)}/trials/${e}/comparison`)}artifacts(n){return this.get(`/campaigns/${encodeURIComponent(n)}/artifacts`)}log(n,e,i){return this.get(`/campaigns/${encodeURIComponent(n)}/log`,{path:e,offset:i,limit:200})}artifactUrl(n,e){return`${this.base}/campaigns/${encodeURIComponent(n)}/artifact?${new URLSearchParams({path:e})}`}static ɵfac=function(e){return new(e||t)};static ɵprov=he({token:t,factory:t.ɵfac,providedIn:`root`})};var Om=class t{client=f(Am);destroy=f(Ne);config=M(null);campaigns=M([]);active=M(``);selectedId=M(``);selectedTrial=M(1);campaign=ne(()=>this.campaigns().find(n=>n.id===this.selectedId())??null);trial=ne(()=>this.campaign()?.trials.find(n=>n.number===this.selectedTrial())??null);completed=ne(()=>this.campaign()?.trials.filter(n=>[`SUCCEEDED`,`FAILED`].includes(n.status)).length??0);progress=M(null);tuning=ne(()=>this.progress()?.budget?this.progress():null);comparison=M(null);artifacts=M([]);logPath=M(``);logLines=M([]);error=M(null);connectionError=M(null);busy=M(!1);timer=null;started=!1;destroyed=!1;logOffset=0;constructor(){this.destroy.onDestroy(()=>{this.destroyed=!0,this.timer&&clearTimeout(this.timer)})}async start(){if(!this.started){this.started=!0;try{this.config.set(await this.client.defaults())}catch(n){this.connectionError.set(this.message(n))}await this.poll()}}async poll(){if(!this.destroyed)try{this.config()||this.config.set(await this.client.defaults());let n=await this.client.list();if(this.destroyed)return;if(this.connectionError.set(null),this.campaigns.set(n.campaigns),this.active.set(n.active),this.selectedId()||this.selectedId.set(n.active||n.campaigns[0]?.id||``),this.trial()){let e=this.selectedId(),i=this.selectedTrial(),[a,o]=await Promise.all([this.client.progress(e,i),this.client.comparison(e,i)]);this.selectedId()===e&&this.selectedTrial()===i&&!this.destroyed&&(this.progress.set(a),this.comparison.set(o))}if(this.logPath()){let e=this.selectedId(),i=this.logPath(),a=this.logOffset,o=await this.client.log(e,i,a);e===this.selectedId()&&i===this.logPath()&&a===this.logOffset&&!this.destroyed&&(this.logOffset=o.nextOffset,this.logLines.update(r=>[...r,...o.lines].slice(-1e3)))}}catch(n){this.destroyed||this.connectionError.set(this.message(n))}finally{this.destroyed||(this.timer=setTimeout(()=>{this.poll()},2e3))}}select(n){this.selectedId.set(n),this.selectedTrial.set(1),this.artifacts.set([]),this.selectLog(``),this.progress.set(null),this.comparison.set(null)}selectTrial(n){this.selectedTrial.set(n),this.progress.set(null),this.comparison.set(null),this.selectLog(``)}selectLog(n){this.logPath.set(n),this.logOffset=0,this.logLines.set([])}async refreshArtifacts(){if(!this.selectedId())return;let n=this.selectedId();try{let e=await this.client.artifacts(n);n===this.selectedId()&&!this.destroyed&&this.artifacts.set(e)}catch(e){this.error.set(this.message(e))}}async createAndStart(){let n=this.config();n&&await this.operate(async()=>{let e=await this.client.validate(n);if(!e.valid)throw new Error(e.errors.join(`; `));let i=await this.client.create(n);this.campaigns.update(a=>[i,...a]),this.select(i.id),await this.client.action(i.id,`start`),this.active.set(i.id)})}async action(n){await this.operate(async()=>{await this.client.action(this.selectedId(),n),(n===`start`||n===`resume`)&&this.active.set(this.selectedId())})}async operate(n){this.busy.set(!0),this.error.set(null);try{await n()}catch(e){this.error.set(this.message(e))}finally{this.busy.set(!1)}}artifactUrl(n){return this.client.artifactUrl(this.selectedId(),n)}message(n){let e=n;return e.error?.message??e.message??`AutoLLM request failed`}static ɵfac=function(e){return new(e||t)};static ɵprov=he({token:t,factory:t.ɵfac,providedIn:`root`})};var YF=()=>[`INTERRUPTED`,`CANCELLED`];var KF=(t,n)=>n.id;var nE=(t,n)=>n.number;var QF=(t,n)=>n.path;var XF=(t,n)=>n.threadId;function ZF(t,n){if(t&1&&(l(0,`p`,1),p(1),d()),t&2){let e=_();h(),w(e.store.error())}}function JF(t,n){if(t&1&&(l(0,`p`,1),p(1),d()),t&2){let e=_();h(),w(e.store.connectionError())}}function e2(t,n){if(t&1){let e=ae();l(0,`mat-card`)(1,`mat-card-content`)(2,`details`,5)(3,`summary`),p(4,`Configure a campaign`),d(),l(5,`fieldset`,6)(6,`div`,7)(7,`label`),p(8,`Baseline repository`),l(9,`input`,2),C(`change`,function(a){A(e);return O(_().update(`project.repository`,a))}),d()(),l(10,`label`),p(11,`Git ref`),l(12,`input`,2),C(`change`,function(a){A(e);return O(_().update(`project.ref`,a))}),d()(),l(13,`label`),p(14,`Build command (JSON arguments)`),l(15,`input`,2),Q(16,`json`),C(`change`,function(a){A(e);return O(_().buildCommand(a))}),d()(),l(17,`label`),p(18,`Built JAR path`),l(19,`input`,2),C(`change`,function(a){A(e);return O(_().update(`project.artifact`,a))}),d()(),l(20,`label`),p(21,`SOTA component JSON`),l(22,`input`,2),C(`change`,function(a){A(e);return O(_().update(`sotaFile`,a))}),d()(),l(23,`label`),p(24,`Additional seeds (optional)`),l(25,`input`,2),C(`change`,function(a){A(e);return O(_().update(`initialConfigurationsFile`,a))}),d()(),l(26,`label`),p(27,`Training instances`),l(28,`input`,2),C(`change`,function(a){A(e);return O(_().update(`trainingPath`,a))}),d()(),l(29,`label`),p(30,`Comparison instances`),l(31,`input`,2),C(`change`,function(a){A(e);return O(_().update(`comparisonPath`,a))}),d()(),l(32,`label`),p(33,`Independent trials`),l(34,`input`,8),C(`change`,function(a){A(e);return O(_().update(`trials`,a,!0))}),d()(),l(35,`label`),p(36,`Campaign seed`),l(37,`input`,9),C(`change`,function(a){A(e);return O(_().update(`seed`,a,!0))}),d()(),l(38,`label`),p(39,`Parameter limit`),l(40,`input`,10),C(`change`,function(a){A(e);return O(_().update(`maxParameters`,a,!0))}),d()(),l(41,`label`),p(42,`Workers`),l(43,`input`,8),C(`change`,function(a){A(e);return O(_().update(`execution.workers`,a,!0))}),d()(),l(44,`label`),p(45,`Main evaluations`),l(46,`input`,8),C(`change`,function(a){A(e);return O(_().update(`tuning.evaluations`,a,!0))}),d()(),l(47,`label`),p(48,`Comparison repetitions`),l(49,`input`,8),C(`change`,function(a){A(e);return O(_().update(`comparison.repetitions`,a,!0))}),d()(),l(50,`label`),p(51,`Pilot duration (milliseconds)`),l(52,`input`,8),C(`change`,function(a){A(e);return O(_().update(`preparation.pilotDurationMillis`,a,!0))}),d()(),l(53,`label`),p(54,`Repair turns`),l(55,`input`,11),C(`change`,function(a){A(e);return O(_().update(`preparation.maxRepairs`,a,!0))}),d()(),l(56,`label`),p(57,`Codex model (optional)`),l(58,`input`,2),C(`change`,function(a){A(e);return O(_().update(`codex.model`,a))}),d()(),l(59,`label`),p(60,`Reasoning effort (optional)`),l(61,`input`,2),C(`change`,function(a){A(e);return O(_().update(`codex.reasoningEffort`,a))}),d()(),l(62,`label`),p(63,`Custom prompt file (optional)`),l(64,`input`,2),C(`change`,function(a){A(e);return O(_().update(`codex.promptFile`,a))}),d()()(),l(65,`label`),p(66,`Prompt override (optional)`),l(67,`textarea`,12),C(`change`,function(a){A(e);return O(_().update(`codex.prompt`,a))}),d()(),l(68,`details`)(69,`summary`),p(70,`All settings, including time budgets and timeouts`),d(),l(71,`textarea`,13),Q(72,`json`),C(`change`,function(a){A(e);return O(_().advanced(a))}),d()()(),l(73,`p`),p(74,`Each trial starts from the same baseline. Both comparison algorithms restart within their time budget. Ablation is disabled.`),d(),l(75,`button`,14),C(`click`,function(){A(e);return O(_().store.createAndStart())}),p(76,`Create and start campaign`),d()()()()}if(t&2){let e=n,i=_();h(2),S(`open`,!i.store.campaign()),h(3),S(`disabled`,!!i.store.active()||i.store.busy()),h(4),S(`value`,e.project.repository),h(3),S(`value`,e.project.ref),h(3),S(`value`,Ce(16,24,e.project.buildCommand)),h(4),S(`value`,e.project.artifact),h(3),S(`value`,e.sotaFile??``),h(3),S(`value`,e.initialConfigurationsFile??``),h(3),S(`value`,e.trainingPath??``),h(3),S(`value`,e.comparisonPath??``),h(3),S(`value`,e.trials),h(3),S(`value`,e.seed),h(3),S(`value`,e.maxParameters),h(3),S(`value`,e.execution.workers),h(3),S(`value`,e.tuning.evaluations),h(3),S(`value`,e.comparison.repetitions),h(3),S(`value`,e.preparation.pilotDurationMillis),h(3),S(`value`,e.preparation.maxRepairs),h(3),S(`value`,e.codex.model??``),h(3),S(`value`,e.codex.reasoningEffort??``),h(3),S(`value`,e.codex.promptFile??``),h(3),S(`value`,e.codex.prompt??``),h(4),S(`value`,Ce(72,26,e)),h(4),S(`disabled`,!!i.store.active()||i.store.busy())}}function t2(t,n){if(t&1&&(l(0,`option`,4),p(1),Q(2,`date`),d()),t&2){let e=n.$implicit;S(`value`,e.id),h(),Rn(``,He(2,4,e.createdAt,`short`),` — `,e.status,` — `,e.id)}}function n2(t,n){if(t&1){let e=ae();l(0,`button`,24),C(`click`,function(){A(e);return O(_(2).store.action(`cancel`))}),p(1,`Cancel campaign`),d()}if(t&2)S(`disabled`,_(2).store.busy())}function i2(t,n){if(t&1){let e=ae();l(0,`button`,14),C(`click`,function(){A(e);return O(_(2).store.action(`resume`))}),p(1,`Resume from checkpoints`),d()}if(t&2)S(`disabled`,_(2).store.busy())}function a2(t,n){if(t&1){let e=ae();l(0,`button`,25),C(`click`,function(){A(e);return O(_(2).store.action(`start`))}),p(1,`Start saved draft`),d()}}function o2(t,n){if(t&1){let e=ae();l(0,`button`,24),C(`click`,function(){A(e);return O(_(2).store.action(`report`))}),p(1,`Regenerate reports`),d()}if(t&2)S(`disabled`,_(2).store.busy())}function r2(t,n){t&1&&p(0,` Known totals may be incomplete. `)}function s2(t,n){if(t&1&&(l(0,`p`,20),p(1),Q(2,`number`),Q(3,`number`),Q(4,`number`),d(),l(5,`p`),p(6),N(7,r2,1,0),d()),t&2){let e=n;h(),qi(`Codex token usage · `,e.status,` · Input: `,e.tokens.inputTokens===null?`Unknown`:Ce(2,9,e.tokens.inputTokens),` · Output: `,e.tokens.outputTokens===null?`Unknown`:Ce(3,11,e.tokens.outputTokens),` · Total: `,e.tokens.totalTokens===null?`Unknown`:Ce(4,13,e.tokens.totalTokens)),h(5),qi(``,e.completeAttempts,`/`,e.attempts,` attempts have complete usage; `,e.partialAttempts,` partial, `,e.unavailableAttempts,` unavailable. Includes repairs and failed or interrupted attempts. `),h(),T(e.status===`PARTIAL`?7:-1)}}function c2(t,n){t&1&&(l(0,`p`,20),p(1,`Codex token usage · UNAVAILABLE`),d())}function l2(t,n){if(t&1&&(l(0,`pre`,21),p(1),d()),t&2){let e=_();h(),w(e.failure)}}function d2(t,n){if(t&1&&(l(0,`details`)(1,`summary`),p(2,`Report export failed`),d(),l(3,`pre`,21),p(4),d()()),t&2){let e=_();h(4),w(e.reportFailure)}}function u2(t,n){if(t&1){let e=ae();l(0,`tr`)(1,`td`),p(2),d(),l(3,`td`),p(4),d(),l(5,`td`),p(6),d(),l(7,`td`),p(8),d(),l(9,`td`)(10,`button`,22),C(`click`,function(){let a=A(e).$implicit;return O(_(2).store.selectTrial(a.number))}),p(11,`Open trial`),d()()()}if(t&2){let e=n.$implicit;h(2),w(e.number),h(2),w(e.status),h(2),w(e.tuningSeed),h(2),w(e.repairTurns)}}function m2(t,n){if(t&1&&(l(0,`p`),p(1),Q(2,`number`),Q(3,`number`),d()),t&2){let e=n;h(),Rn(`Trial token usage · `,e.status,` · Input: `,e.tokens.inputTokens===null?`Unknown`:Ce(2,3,e.tokens.inputTokens),` · Output: `,e.tokens.outputTokens===null?`Unknown`:Ce(3,5,e.tokens.outputTokens))}}function p2(t,n){if(t&1&&(l(0,`p`),p(1),d(),z(2,`mat-progress-bar`,19)),t&2){let e=n;h(),qd(``,e.phase,` · `,e.budget.used,`/`,e.budget.maximum,` evaluations · `,e.evaluations.succeeded,` succeeded · `,e.evaluations.rejected,` rejected · `,e.evaluations.failed,` failed`),h(),S(`value`,100*e.budget.used/e.budget.maximum)}}function h2(t,n){if(t&1&&(l(0,`pre`,21),p(1),d()),t&2){let e=_();h(),w(e.failure)}}function f2(t,n){if(t&1&&(l(0,`a`,28),p(1),d()),t&2){let e=n.$implicit,i=_(2).$implicit;S(`href`,_(3).store.artifactUrl(i.directory+`/`+e.path),Cn),h(),H(`Download conversation `,e.threadId)}}function g2(t,n){if(t&1&&(l(0,`span`),p(1),d(),ve(2,f2,2,2,`a`,28,XF)),t&2){let e=n;h(),Ue(`Conversation: `,e.status,` · Usage: `,e.usage.status),h(),ye(e.conversations)}}function b2(t,n){if(t&1&&(l(0,`details`)(1,`summary`),p(2,`Failure details`),d(),l(3,`pre`),p(4),d()()),t&2){let e=_().$implicit;h(4),w(e.failure)}}function v2(t,n){if(t&1){let e=ae();l(0,`div`,26)(1,`span`),p(2),Q(3,`date`),d(),l(4,`button`,22),C(`click`,function(){let a=A(e).$implicit;return O(_(3).store.selectLog(a.directory+([`PREPARATION`].includes(a.stage)?`/build`:``)+`/stdout.log`))}),p(5,`View log`),d(),N(6,g2,4,2),N(7,b2,5,1,`details`),d()}if(t&2){let e,i=n.$implicit;h(2),Rn(``,i.stage,` · `,i.status,` · `,He(3,5,i.startedAt,`short`)),h(4),T((e=i.codexEvidence)?6:-1,e),h(),T(i.failure?7:-1)}}function y2(t,n){if(t&1&&(l(0,`h3`),p(1,`First-ranked proposal: components and parameters`),d(),l(2,`p`),p(3,`Generated algorithm components end in New.`),d(),l(4,`pre`),p(5),Q(6,`json`),d()),t&2){let e=_();h(5),w(Ce(6,1,e.winner))}}function _2(t,n){if(t&1&&(l(0,`details`,27)(1,`summary`),p(2,`Comparison results`),d(),l(3,`pre`),p(4),Q(5,`json`),d()()),t&2){let e=_(3);h(4),w(Ce(5,1,e.comparisonSummary()))}}function x2(t,n){if(t&1&&(l(0,`option`,4),p(1),d()),t&2){let e=n.$implicit;S(`value`,e),h(),w(e)}}function C2(t,n){if(t&1&&(l(0,`option`,4),p(1),d()),t&2){let e=n.$implicit;S(`value`,e),h(),w(e)}}function S2(t,n){if(t&1){let e=ae();l(0,`div`,7)(1,`label`),p(2,`Convergence instance`),l(3,`select`,2),C(`change`,function(a){A(e);return O(_(3).selectedInstance.set(a.target.value))}),ve(4,x2,2,2,`option`,4,ii),d()(),l(6,`label`),p(7,`Objective`),l(8,`select`,2),C(`change`,function(a){A(e);return O(_(3).selectedObjective.set(a.target.value))}),ve(9,C2,2,2,`option`,4,ii),d()()(),z(11,`app-dashboard-chart`,29)}if(t&2){let e=_(3);h(3),S(`value`,e.selectedInstance()||e.instances()[0]),h(),ye(e.instances()),h(4),S(`value`,e.selectedObjective()||e.objectives()[0]),h(),ye(e.objectives()),h(2),S(`model`,e.curveChart())}}function w2(t,n){if(t&1&&(l(0,`mat-card`)(1,`mat-card-content`)(2,`h2`),p(3),d(),N(4,m2,4,7,`p`),N(5,p2,3,7),N(6,h2,2,1,`pre`,21),ve(7,v2,8,8,`div`,26,nE),N(9,y2,7,3),N(10,_2,6,3,`details`,27),N(11,S2,12,3),d()()),t&2){let e,i,a=n,o=_(2);h(3),Ue(`Trial `,a.number,` · `,a.status),h(),T((e=a.codexUsage)?4:-1,e),h(),T((i=o.store.tuning())?5:-1,i),h(),T(a.failure?6:-1),h(),ye(a.attempts),h(2),T(a.winner?9:-1),h(),T(o.store.comparison()?10:-1),h(),T(o.instances().length?11:-1)}}function E2(t,n){if(t&1&&(l(0,`mat-card`)(1,`mat-card-content`)(2,`h2`),p(3),d(),l(4,`pre`,30),p(5),d()()()),t&2){let e=_(2);h(3),w(e.store.logPath()),h(2),w(e.store.logLines().join(`
`))}}function D2(t,n){if(t&1){let e=ae();l(0,`button`,22),C(`click`,function(){A(e);let a=_().$implicit;return O(_(2).store.selectLog(a.path))}),p(1,`View log`),d()}}function k2(t,n){if(t&1&&(l(0,`div`,23)(1,`a`,28),p(2),d(),l(3,`span`),p(4),d(),N(5,D2,2,0,`button`,31),d()),t&2){let e=n.$implicit,i=_(2);h(),S(`href`,i.store.artifactUrl(e.path),Cn),h(),w(e.path),h(2),H(``,e.size,` bytes`),h(),T(e.path.endsWith(`.log`)||e.path.endsWith(`.jsonl`)?5:-1)}}function I2(t,n){if(t&1){let e=ae();l(0,`mat-card`)(1,`mat-card-content`)(2,`div`,15)(3,`strong`),p(4),d(),N(5,n2,2,1,`button`,16),N(6,i2,2,1,`button`,17),N(7,a2,2,0,`button`,18),N(8,o2,2,1,`button`,16),d(),z(9,`mat-progress-bar`,19),N(10,s2,8,15)(11,c2,2,0,`p`,20),N(12,l2,2,1,`pre`,21),N(13,d2,5,1,`details`),l(14,`table`)(15,`thead`)(16,`tr`)(17,`th`),p(18,`Trial`),d(),l(19,`th`),p(20,`Stage / outcome`),d(),l(21,`th`),p(22,`Tuning seed`),d(),l(23,`th`),p(24,`Repairs`),d(),l(25,`th`),p(26,`Inspect`),d()()(),l(27,`tbody`),ve(28,u2,12,4,`tr`,null,nE),d()()()(),N(30,w2,12,8,`mat-card`),N(31,E2,6,2,`mat-card`),l(32,`mat-card`)(33,`mat-card-content`)(34,`div`,15)(35,`h2`),p(36,`Saved data and artifacts`),d(),l(37,`button`,22),C(`click`,function(){A(e);return O(_().store.refreshArtifacts())}),p(38,`Refresh artifacts`),d()(),ve(39,k2,6,4,`div`,23,QF),l(41,`details`)(42,`summary`),p(43,`Frozen campaign settings and versions`),d(),l(44,`pre`),p(45),Q(46,`json`),d(),l(47,`pre`),p(48),Q(49,`json`),d()()()()}if(t&2){let e,i,a=n,o=_();h(4),Rn(``,a.status,` · `,o.store.completed(),`/`,a.trials.length,` trials finished`),h(),T(o.store.active()===a.id?5:-1),h(),T(Bt(19,YF).includes(a.status)&&!o.store.active()?6:-1),h(),T(a.status===`DRAFT`&&!o.store.active()?7:-1),h(),T(a.status!==`DRAFT`&&!o.store.active()?8:-1),h(),S(`value`,100*o.store.completed()/a.trials.length),h(),T((e=a.codexUsage)?10:11,e),h(2),T(a.failure?12:-1),h(),T(a.reportFailure?13:-1),h(15),ye(a.trials),h(2),T((i=o.store.trial())?30:-1,i),h(),T(o.store.logPath()?31:-1),h(8),ye(o.store.artifacts()),h(6),w(Ce(46,15,a.config)),h(3),w(Ce(49,17,a.versions))}}var Rm=class t$1{store=f(Om);selectedInstance=M(``);selectedObjective=M(``);curves=ne(()=>this.store.comparison()?.curves??[]);instances=ne(()=>[...new Set(this.curves().map(n=>n.instance))]);objectives=ne(()=>[...new Set(this.curves().flatMap(n=>Object.keys(n.curves)))]);comparisonSummary=ne(()=>{let i=this.store.comparison()??{},{curves:n}=i;return t(i,[`curves`])});curveChart=ne(()=>{let n=this.instances().includes(this.selectedInstance())?this.selectedInstance():this.instances()[0],e=this.objectives().includes(this.selectedObjective())?this.selectedObjective():this.objectives()[0];return{options:{chart:{type:`line`,height:360},title:{text:e??`Comparison convergence`},xAxis:{title:{text:`Time (milliseconds)`}},yAxis:{title:{text:`Objective value`}},series:this.curves().filter(i=>i.instance===n).map(i=>({type:`line`,name:`${i.algorithm} \xB7 repetition ${Number(i.iteration)+1}`,data:(i.curves[e]??[]).filter(a=>Number.isFinite(a.value)).map(a=>[a.instant/1e6,a.value])}))}}});constructor(){this.store.start()}update(n,e,i=!1){let a=this.store.config();if(!a)return;let o=structuredClone(a),r=o,s=n.split(`.`);for(let m of s.slice(0,-1))r=r[m];let c=e.target.value;r[s[s.length-1]]=i?Number(c):c||null,this.store.config.set(o)}buildCommand(n){try{let e=JSON.parse(n.target.value);if(!Array.isArray(e)||e.some(i=>typeof i!=`string`))throw new Error(`Build command must be a JSON array of arguments`);this.store.config.update(i=>i&&s(r({},i),{project:s(r({},i.project),{buildCommand:e})}))}catch(e){this.store.error.set(e.message)}}advanced(n){try{let e=this.store.config();if(!e)return;let i=JSON.parse(n.target.value);if(!i||typeof i!=`object`||Array.isArray(i))throw new Error(`Settings must be a JSON object`);for(let a of[`project`,`codex`,`preparation`,`tuning`,`comparison`,`execution`])if(i[a]!=null&&(typeof i[a]!=`object`||Array.isArray(i[a])))throw new Error(`${a} must be a JSON object`);this.store.config.set(s(r(r({},e),i),{project:r(r({},e.project),i.project),codex:r(r({},e.codex),i.codex),preparation:r(r({},e.preparation),i.preparation),tuning:r(r({},e.tuning),i.tuning),comparison:r(r({},e.comparison),i.comparison),execution:r(r({},e.execution),i.execution)})),this.store.error.set(null)}catch(e){this.store.error.set(e.message)}}static ɵfac=function(e){return new(e||t$1)};static ɵcmp=B({type:t$1,selectors:[[`app-autollm-dashboard`]],decls:19,vars:5,consts:[[`src`,`favicon.svg`,`alt`,`Mork`],[`role`,`alert`,1,`error`],[3,`change`,`value`],[`value`,``],[3,`value`],[3,`open`],[3,`disabled`],[1,`fields`],[`type`,`number`,`min`,`1`,3,`change`,`value`],[`type`,`number`,3,`change`,`value`],[`type`,`number`,`min`,`1`,`max`,`299`,3,`change`,`value`],[`type`,`number`,`min`,`0`,3,`change`,`value`],[`rows`,`6`,3,`change`,`value`],[`rows`,`16`,3,`change`,`value`],[`mat-flat-button`,``,3,`click`,`disabled`],[1,`actions`],[`mat-button`,``,3,`disabled`],[`mat-flat-button`,``,3,`disabled`],[`mat-flat-button`,``],[`mode`,`determinate`,3,`value`],[1,`codex-usage`],[1,`error`],[`mat-button`,``,3,`click`],[1,`artifact`],[`mat-button`,``,3,`click`,`disabled`],[`mat-flat-button`,``,3,`click`],[1,`attempt`],[`open`,``],[3,`href`],[3,`model`],[1,`logs`],[`mat-button`,``]],template:function(e,i){if(e&1&&(l(0,`main`)(1,`header`),z(2,`img`,0),l(3,`div`)(4,`h1`),p(5,`AutoLLM campaigns`),d(),l(6,`p`),p(7,`Generate components, tune algorithms, and compare each independent proposal against SOTA.`),d()()(),N(8,ZF,2,1,`p`,1),N(9,JF,2,1,`p`,1),N(10,e2,77,28,`mat-card`),l(11,`label`),p(12,`Saved campaign`),l(13,`select`,2),C(`change`,function(o){return i.store.select(o.target.value)}),l(14,`option`,3),p(15,`Select campaign`),d(),ve(16,t2,3,7,`option`,4,KF),d()(),N(18,I2,50,20),d()),e&2){let a,o;h(8),T(i.store.error()?8:-1),h(),T(i.store.connectionError()?9:-1),h(),T((a=i.store.config())?10:-1,a),h(3),S(`value`,i.store.selectedId()),h(3),ye(i.store.campaigns()),h(2),T((o=i.store.campaign())?18:-1,o)}},dependencies:[Tt,Zt,jn,Vn,Bn,as,is,Ti,xo,Qi,Sn],styles:[`[_nghost-%COMP%]{display:block;background:var(--%NS%mat-sys-surface);color:var(--%NS%mat-sys-on-surface);min-height:100vh}main[_ngcontent-%COMP%]{max-width:88rem;margin:auto;padding:1.5rem;display:grid;gap:1rem}header[_ngcontent-%COMP%], .actions[_ngcontent-%COMP%], .artifact[_ngcontent-%COMP%]{display:flex;align-items:center;flex-wrap:wrap;gap:1rem}header[_ngcontent-%COMP%]   img[_ngcontent-%COMP%]{width:3rem}fieldset[_ngcontent-%COMP%]{border:0;padding:1rem 0}.fields[_ngcontent-%COMP%]{display:grid;grid-template-columns:repeat(auto-fit,minmax(16rem,1fr));gap:1rem}label[_ngcontent-%COMP%]{display:flex;flex-direction:column;gap:.4rem;font-weight:500;margin-bottom:.6rem}input[_ngcontent-%COMP%], textarea[_ngcontent-%COMP%], select[_ngcontent-%COMP%]{padding:.6rem;background:var(--%NS%mat-sys-surface-container);color:inherit;border:1px solid var(--%NS%mat-sys-outline);border-radius:.4rem;font:inherit}textarea[_ngcontent-%COMP%]{width:100%;box-sizing:border-box}summary[_ngcontent-%COMP%]{cursor:pointer;padding:.5rem 0}table[_ngcontent-%COMP%]{width:100%;border-collapse:collapse;margin:1rem 0}th[_ngcontent-%COMP%], td[_ngcontent-%COMP%]{text-align:left;padding:.6rem;border-bottom:1px solid var(--%NS%mat-sys-outline-variant)}pre[_ngcontent-%COMP%]{white-space:pre-wrap;overflow-wrap:anywhere;overflow:auto;max-height:25rem;padding:.75rem;background:var(--%NS%mat-sys-surface-container)}.logs[_ngcontent-%COMP%]{white-space:pre}.error[_ngcontent-%COMP%]{color:var(--%NS%mat-sys-error)}.attempt[_ngcontent-%COMP%]{padding:.5rem 0}.artifact[_ngcontent-%COMP%]{padding:.25rem 0}.artifact[_ngcontent-%COMP%]   a[_ngcontent-%COMP%]{overflow-wrap:anywhere}`]})};var Pm=class t{client=f(Pa);destroyRef=f(Ne);viewMode=M(`detecting`);latestError=M(null);timer=null;started=!1;destroyed=!1;polling=!1;retry=!1;delay=500;constructor(){this.destroyRef.onDestroy(()=>{this.destroyed=!0,this.timer&&clearTimeout(this.timer)})}start(){this.started||(this.started=!0,this.poll())}retryNow(){this.timer&&clearTimeout(this.timer),this.polling?this.retry=!0:this.poll()}async poll(){if(this.destroyed)return;this.polling=!0;let n=1e4;try{let e=await this.client.getMode();if(this.destroyed)return;this.viewMode.set(e.mode.toLowerCase()),this.latestError.set(null),this.delay=500,e.mode===`DETECTING`&&(n=500)}catch(e){if(this.destroyed)return;this.latestError.set(e instanceof Error?e.message:`Could not contact the backend`),this.viewMode()===`detecting`&&this.viewMode.set(`unavailable`),n=this.delay,this.delay=Math.min(3e4,this.delay*2)}finally{this.polling=!1,this.destroyed||(this.timer=setTimeout(()=>{this.poll()},this.retry?0:n),this.retry=!1)}}static ɵfac=function(e){return new(e||t)};static ɵprov=he({token:t,factory:t.ɵfac,providedIn:`root`})};function M2(t,n){t&1&&z(0,`app-autollm-dashboard`)}function N2(t,n){t&1&&z(0,`app-standard-dashboard`)}function T2(t,n){t&1&&z(0,`app-tuning-dashboard`)}function A2(t,n){t&1&&(l(0,`main`,0),z(1,`img`,2),l(2,`mat-card`,3)(3,`mat-card-header`)(4,`mat-card-title`),p(5,`Autoconfig worker`),d()(),l(6,`mat-card-content`)(7,`p`),p(8,`This process executes evaluations for the coordinator.`),d(),l(9,`p`),p(10,`The monitoring glass panel is available on the coordinator process.`),d()()()())}function O2(t,n){t&1&&(l(0,`main`,0),z(1,`img`,2),l(2,`mat-card`)(3,`mat-card-content`)(4,`h1`),p(5,`Instance utility`),d(),l(6,`p`),p(7,`This process is analysing or selecting instances. It is not running a measured experiment.`),d()()()())}function R2(t,n){if(t&1){let e=ae();l(0,`main`,0),z(1,`img`,2),l(2,`mat-card`,3)(3,`mat-card-header`)(4,`mat-card-title`),p(5,`Waiting for the Mork backend`),d()(),l(6,`mat-card-content`)(7,`p`),p(8),d(),l(9,`button`,4),C(`click`,function(){A(e);return O(_().store.retryNow())}),p(10,`Retry now`),d()()()()}if(t&2){let e=_();h(8),w(e.store.latestError()??`The run mode could not be detected.`)}}function P2(t,n){t&1&&(l(0,`main`,1),z(1,`img`,2)(2,`mat-spinner`,5),l(3,`p`),p(4,`Detecting experiment mode…`),d()())}bg(class t{store=f(Pm);ngOnInit(){this.store.start()}static ɵfac=function(e){return new(e||t)};static ɵcmp=B({type:t,selectors:[[`app-root`]],decls:7,vars:1,consts:[[1,`mode-state`],[`aria-label`,`Detecting experiment mode`,1,`mode-state`,`detecting`],[`src`,`favicon.svg`,`alt`,`Mork`],[`appearance`,`outlined`],[`mat-flat-button`,``,`type`,`button`,3,`click`],[`diameter`,`36`]],template:function(e,i){if(e&1&&N(0,M2,1,0,`app-autollm-dashboard`)(1,N2,1,0,`app-standard-dashboard`)(2,T2,1,0,`app-tuning-dashboard`)(3,A2,11,0,`main`,0)(4,O2,8,0,`main`,0)(5,R2,11,1,`main`,0)(6,P2,5,0,`main`,1),e&2){let a;T((a=i.store.viewMode())===`autollm`?0:a===`standard`?1:a===`tuning`?2:a===`worker`?3:a===`utility`?4:a===`unavailable`?5:6)}},dependencies:[Tt,Zt,jn,Vn,Bn,Ia,ka,nS,tS,gm,Tm,Rm],styles:[`[_nghost-%COMP%]{display:block;min-height:100vh}.mode-state[_ngcontent-%COMP%]{display:grid;place-content:center;min-height:100vh;padding:1rem;color:var(--%NS%mat-sys-on-surface);background:radial-gradient(circle at 50% 20%,color-mix(in srgb,var(--%NS%mat-sys-tertiary) 16%,transparent),transparent 25rem),var(--%NS%mat-sys-surface-container-lowest)}.mode-state[_ngcontent-%COMP%] > img[_ngcontent-%COMP%]{width:5rem;height:5rem;margin:0 auto 1.25rem}.mode-state[_ngcontent-%COMP%]   mat-card[_ngcontent-%COMP%]{width:min(32rem,100vw - 2rem)}.mode-state.detecting[_ngcontent-%COMP%]{justify-items:center;color:var(--%NS%mat-sys-on-surface-variant)}.mode-state.detecting[_ngcontent-%COMP%] > img[_ngcontent-%COMP%]{margin-bottom:1.5rem}`]})},bC).catch(t=>console.error(t));export{s as n,r as t};