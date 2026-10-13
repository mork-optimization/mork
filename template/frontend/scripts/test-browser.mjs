import assert from 'node:assert/strict';
import { spawn } from 'node:child_process';
import { createHash } from 'node:crypto';
import { access, mkdtemp, readFile, writeFile } from 'node:fs/promises';
import { createServer } from 'node:http';
import { tmpdir } from 'node:os';
import { extname, join, resolve } from 'node:path';

// Production-bundle smoke test, using Chromium's native CDP and no extra npm dependencies.
// MORK_MONITOR_URL optionally tests a running real standard-experiment backend instead.
const artifacts = await mkdtemp(join(tmpdir(), 'mork-normal-browser-'));
const root = resolve(process.env.MORK_STATIC_ROOT ?? 'dist');
const candidates = [
  process.env.MORK_BROWSER,
  '/usr/bin/google-chrome',
  '/usr/bin/chromium',
  '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
  `${process.env.HOME}/Library/Caches/ms-playwright/chromium_headless_shell-1228/chrome-headless-shell-mac-arm64/chrome-headless-shell`,
].filter(Boolean);
let executable;
for (const candidate of candidates) {
  try {
    await access(candidate);
    executable = candidate;
    break;
  } catch {}
}
assert.ok(executable, 'Chromium is required. Set MORK_BROWSER to its executable.');

const counts = { planned: 6, pending: 0, running: 0, succeeded: 5, failed: 1, cancelled: 0 };
const status = {
  runId: 'browser-run',
  revision: 1,
  phase: 'RUNNING',
  startedAt: Date.now() - 20000,
  endedAt: null,
  elapsedMillis: 20000,
  currentExperiment: 'Browser experiment',
  objectives: { Cost: 'MINIMIZE', Quality: 'MAXIMIZE' },
  counts: { planned: 72, pending: 50, running: 1, succeeded: 20, failed: 1, cancelled: 0 },
  instances: 12,
  completedInstances: 3,
  runningInstances: 1,
  algorithms: 24,
  completedAlgorithms: 6,
  runningAlgorithms: 1,
  experiments: [{ name: 'Browser experiment', counts, instances: 12, completedInstances: 3 }],
  issueCount: 1,
};
const instances = Array.from({ length: 12 }, (_, index) => ({
  id: 'instance-' + index,
  experimentName: status.currentExperiment,
  instanceName: 'Instance ' + index,
  counts,
  references: { Cost: 1, Quality: 10 },
  algorithms: 2,
  completedAlgorithms: 2,
  finalized: true,
  revision: 1,
}));
const resultRows = instances.flatMap((instance) =>
  ['Algorithm A', 'Algorithm B'].flatMap((algorithmName, algorithm) =>
    Array.from({ length: 3 }, (_, i) => ({
      resultId: `${instance.id}-${algorithm}-${i}`,
      instanceId: instance.id,
      experimentName: instance.experimentName,
      instanceName: instance.instanceName,
      algorithmName,
      ordinal: i + 1,
      iteration: 'rep-' + (i + 1),
      seed: 100 + i,
      state: algorithm === 0 && i === 1 ? 'FAILED' : 'SUCCEEDED',
      objectives:
        algorithm === 0 && i === 1 ? {} : { Cost: 2 + algorithm + i, Quality: 8 - algorithm - i },
      properties: { fixture: true },
      executionTime: 2000000 + i * 1000000,
      timeToBest: 1000000,
      startedAt: 1,
      endedAt: 2,
    })),
  ),
);
const issue = {
  id: 1,
  timestamp: Date.now(),
  severity: 'ERROR',
  scope: 'COMPUTATION',
  exceptionType: 'IllegalStateException',
  message: 'Fixture computation failed',
  resultId: 'instance-0-0-1',
  experimentName: status.currentExperiment,
  instanceName: 'Instance 0',
  algorithmName: 'Algorithm A',
  iteration: 'rep-2',
  seed: 101,
};
const manifest = {
  runId: status.runId,
  artifacts: [
    {
      id: 'artifact-1',
      filename: 'result.csv',
      experimentName: status.currentExperiment,
      resultId: 'instance-0-0-0',
      size: 7,
      modifiedAt: Date.now(),
    },
  ],
};
function statistics(values, mode, reference) {
  const sorted = [...values].sort((a, b) => a - b),
    n = sorted.length;
  const quantile = (fraction) => {
    const x = (n - 1) * fraction,
      low = Math.floor(x);
    return sorted[low] + (sorted[Math.ceil(x)] - sorted[low]) * (x - low);
  };
  const best = mode === 'MINIMIZE' ? sorted[0] : sorted[n - 1],
    mean = sorted.reduce((a, b) => a + b, 0) / n;
  const gap = reference === null ? null : mode === 'MINIMIZE' ? best - reference : reference - best;
  return {
    count: n,
    best,
    mean,
    median: quantile(0.5),
    standardDeviation: Math.sqrt(sorted.reduce((a, v) => a + (v - mean) ** 2, 0) / (n - 1)),
    lowerQuartile: quantile(0.25),
    upperQuartile: quantile(0.75),
    worst: mode === 'MINIMIZE' ? sorted[n - 1] : sorted[0],
    absoluteGap: gap,
    percentageGap: gap === null ? null : (gap / Math.abs(reference)) * 100,
  };
}
function detail(id, objective) {
  const instance = instances.find((item) => item.id === id),
    results = resultRows.filter((row) => row.instanceId === id);
  return {
    runId: status.runId,
    revision: instance.revision,
    instance,
    objective,
    results,
    resultCount: results.length,
    sampled: false,
    algorithms: ['Algorithm A', 'Algorithm B'].map((name) => {
      const rows = results.filter((row) => row.algorithmName === name && row.state === 'SUCCEEDED');
      rows.sort((a, b) =>
        status.objectives[objective] === 'MINIMIZE'
          ? a.objectives[objective] - b.objectives[objective]
          : b.objectives[objective] - a.objectives[objective],
      );
      return {
        name,
        counts,
        objective: statistics(
          rows.map((row) => row.objectives[objective]),
          status.objectives[objective],
          instance.references[objective],
        ),
        runtimeMillis: statistics(
          rows.map((row) => row.executionTime / 1000000),
          'MINIMIZE',
          null,
        ),
        timeToBestMillis: statistics(
          rows.map(() => 1),
          'MINIMIZE',
          null,
        ),
        bestResultId: rows[0].resultId,
      };
    }),
  };
}
const requests = [],
  sockets = new Set();
let server,
  head = 0,
  unavailable = false;
const page = (items, query) => ({
  runId: status.runId,
  revision: status.revision,
  total: items.length,
  offset: Number(query.get('offset') ?? 0),
  limit: Number(query.get('limit') ?? 25),
  items: items.slice(
    Number(query.get('offset') ?? 0),
    Number(query.get('offset') ?? 0) + Number(query.get('limit') ?? 25),
  ),
});
function frame(socket, text) {
  const body = Buffer.from(text),
    header =
      body.length < 126
        ? Buffer.from([0x81, body.length])
        : Buffer.from([0x81, 126, body.length >> 8, body.length & 255]);
  socket.write(Buffer.concat([header, body]));
}
function notify(type) {
  head++;
  for (const socket of sockets)
    if (socket.subscription)
      frame(
        socket,
        `MESSAGE\nsubscription:${socket.subscription}\nmessage-id:${head}\ndestination:/topic/events\n\n${JSON.stringify({ eventLogId: 'browser-log', eventId: head, type, timestamp: Date.now(), workerName: 'fixture', payload: {} })}\0`,
      );
}
if (!process.env.MORK_MONITOR_URL) {
  server = createServer(async (request, response) => {
    const url = new URL(request.url, 'http://fixture');
    requests.push(url);
    let body;
    if (url.pathname === '/api/monitor/status')
      body = { mode: 'STANDARD', eventLogId: 'browser-log' };
    else if (url.pathname === '/lastevent') body = { eventLogId: 'browser-log', lastEventId: head };
    else if (url.pathname === '/events')
      body = {
        eventLogId: 'browser-log',
        lastEventId: head,
        from: Number(url.searchParams.get('from')),
        to: Number(url.searchParams.get('to')),
        events: [],
      };
    else if (url.pathname === '/api/experiments/status') {
      if (unavailable) {
        response.writeHead(503).end();
        return;
      }
      body = status;
    } else if (url.pathname === '/api/experiments/instances')
      body = page(
        instances.filter((row) =>
          row.instanceName
            .toLowerCase()
            .includes((url.searchParams.get('search') ?? '').toLowerCase()),
        ),
        url.searchParams,
      );
    else if (url.pathname.startsWith('/api/experiments/instances/'))
      body = detail(url.pathname.split('/').at(-1), url.searchParams.get('objective'));
    else if (url.pathname === '/api/experiments/results')
      body = page(
        resultRows.filter(
          (row) =>
            (!url.searchParams.get('state') || row.state === url.searchParams.get('state')) &&
            (!url.searchParams.get('instanceId') ||
              row.instanceId === url.searchParams.get('instanceId')),
        ),
        url.searchParams,
      );
    else if (url.pathname.startsWith('/api/experiments/results/')) {
      const result = resultRows.find((row) => row.resultId === url.pathname.split('/').at(-1));
      body = {
        runId: status.runId,
        revision: status.revision,
        result,
        rendering: { state: 'UNSUPPORTED', rendererId: null, data: {} },
        issues: result.resultId === issue.resultId ? [issue] : [],
      };
    } else if (url.pathname === '/api/experiments/issues') body = page([issue], url.searchParams);
    else if (url.pathname === '/api/experiments/artifacts') body = manifest;
    if (body) {
      response.setHeader('Content-Type', 'application/json');
      response.end(JSON.stringify(body));
      return;
    }
    try {
      const path = url.pathname === '/' ? '/index.html' : url.pathname;
      const content = await readFile(join(root, path));
      response.setHeader(
        'Content-Type',
        {
          '.html': 'text/html',
          '.js': 'application/javascript',
          '.css': 'text/css',
          '.svg': 'image/svg+xml',
        }[extname(path)] ?? 'application/octet-stream',
      );
      response.end(content);
    } catch {
      response.writeHead(404).end();
    }
  });
  server.on('upgrade', (request, socket) => {
    requests.push(new URL(request.url, 'http://fixture'));
    const accept = createHash('sha1')
      .update(request.headers['sec-websocket-key'] + '258EAFA5-E914-47DA-95CA-C5AB0DC85B11')
      .digest('base64');
    socket.write(
      `HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: ${accept}\r\nSec-WebSocket-Protocol: v12.stomp\r\n\r\n`,
    );
    sockets.add(socket);
    socket.on('close', () => sockets.delete(socket));
    socket.on('error', () => {});
    let buffered = Buffer.alloc(0);
    socket.on('data', (chunk) => {
      buffered = Buffer.concat([buffered, chunk]);
      while (buffered.length >= 2) {
        const opcode = buffered[0] & 15,
          masked = buffered[1] & 128;
        let length = buffered[1] & 127,
          start = 2;
        if (length === 126) {
          if (buffered.length < 4) return;
          length = buffered.readUInt16BE(2);
          start = 4;
        }
        if (length === 127) {
          socket.destroy();
          return;
        } // Fixture messages are bounded below 64 KB.
        const mask = masked ? buffered.subarray(start, start + 4) : null;
        if (masked) start += 4;
        if (buffered.length < start + length) return;
        const body = Buffer.from(buffered.subarray(start, start + length));
        buffered = buffered.subarray(start + length);
        if (mask) for (let i = 0; i < body.length; i++) body[i] ^= mask[i % 4];
        if (opcode === 8) {
          socket.end();
          return;
        }
        const text = body.toString();
        if (process.env.MORK_BROWSER_DEBUG) console.log('STOMP fixture:', JSON.stringify(text));
        if (text.startsWith('CONNECT') || text.startsWith('STOMP'))
          frame(socket, 'CONNECTED\nversion:1.2\nheart-beat:0,0\n\n\0');
        if (text.startsWith('SUBSCRIBE')) socket.subscription = text.match(/\nid:([^\n]+)/)?.[1];
        if (text.startsWith('DISCONNECT')) socket.end();
      }
    });
  });
  await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));
}
const address = process.env.MORK_MONITOR_URL ?? `http://127.0.0.1:${server.address().port}`;
const browser = spawn(executable, [
  '--headless=new',
  '--no-sandbox',
  '--remote-debugging-port=0',
  `--user-data-dir=${artifacts}/profile`,
  'about:blank',
]);
let ws;
try {
  const debug = await new Promise((resolve, reject) => {
    let stderr = '';
    browser.stderr.on('data', (chunk) => {
      stderr += chunk;
      const match = stderr.match(/DevTools listening on (ws:\/\/\S+)/);
      if (match) resolve(match[1]);
    });
    browser.on('error', reject);
    browser.on('exit', (code) => reject(new Error(`Browser exited ${code}: ${stderr}`)));
    setTimeout(() => reject(new Error(`Browser startup timed out: ${stderr}`)), 15000).unref();
  });
  const targets = await (await fetch(`http://127.0.0.1:${new URL(debug).port}/json/list`)).json();
  ws = new WebSocket(targets.find((target) => target.type === 'page').webSocketDebuggerUrl);
  await new Promise((resolve, reject) => {
    ws.addEventListener('open', resolve, { once: true });
    ws.addEventListener('error', reject, { once: true });
  });
  let sequence = 0;
  const pending = new Map(),
    errors = [];
  ws.addEventListener('message', ({ data }) => {
    const message = JSON.parse(data);
    if (message.id) {
      const promise = pending.get(message.id);
      pending.delete(message.id);
      message.error
        ? promise.reject(new Error(JSON.stringify(message.error)))
        : promise.resolve(message.result);
    } else if (message.method === 'Runtime.exceptionThrown')
      errors.push(message.params.exceptionDetails);
    else if (message.method === 'Runtime.consoleAPICalled' && message.params.type === 'error')
      errors.push(message.params.args);
  });
  const send = (method, params = {}) =>
    new Promise((resolve, reject) => {
      const id = ++sequence;
      pending.set(id, { resolve, reject });
      ws.send(JSON.stringify({ id, method, params }));
    });
  const evaluate = async (expression) => {
    const result = await send('Runtime.evaluate', {
      expression,
      returnByValue: true,
      awaitPromise: true,
    });
    if (result.exceptionDetails) throw new Error(JSON.stringify(result.exceptionDetails));
    return result.result.value;
  };
  const waitFor = async (expression) => {
    const deadline = Date.now() + 15000;
    while (Date.now() < deadline) {
      if (await evaluate(`(()=>{try{return Boolean(${expression})}catch{return false}})()`)) return;
      await new Promise((resolve) => setTimeout(resolve, 100));
    }
    console.error(
      JSON.stringify({
        errors,
        requests: requests.map((url) => url.pathname),
        page: await evaluate(
          '({text:document.body.textContent, html:document.documentElement.outerHTML.slice(-2000), url:location.href,ready:document.readyState})',
        ),
      }),
    );
    throw new Error(`Timed out: ${expression}`);
  };
  const clickText = (selector, text) =>
    evaluate(
      `Array.from(document.querySelectorAll(${JSON.stringify(selector)})).find(el => el.textContent.trim() === ${JSON.stringify(text)}).click()`,
    );
  await send('Runtime.enable');
  await send('Page.enable');
  await send('Emulation.setDeviceMetricsOverride', {
    width: 1440,
    height: 1000,
    deviceScaleFactor: 1,
    mobile: false,
  });
  await send('Page.navigate', { url: address });
  await waitFor(`document.querySelectorAll('.highcharts-root').length === 4`);
  await waitFor(
    `Array.from(document.querySelectorAll('.highcharts-container')).every(el => getComputedStyle(el).colorScheme === 'light')`,
  );
  assert.equal(await evaluate(`document.querySelectorAll('mat-progress-bar').length`), 3);
  await waitFor(`document.querySelector('mat-chip').textContent.trim() === 'Live'`);
  await writeFile(
    join(artifacts, 'desktop.png'),
    Buffer.from(
      (await send('Page.captureScreenshot', { captureBeyondViewport: true })).data,
      'base64',
    ),
  );
  if (server) {
    assert.ok(
      await evaluate(
        `document.querySelector('.highcharts-boxplot-series .highcharts-point') !== null`,
      ),
      'boxplot module loaded',
    );
    await waitFor(
      `document.querySelector('.highcharts-boxplot-box').getBoundingClientRect().height > 1`,
    );
    assert.equal(
      await evaluate(
        `document.querySelectorAll('.highcharts-series-group .highcharts-scatter-series .highcharts-point').length`,
      ),
      5,
      'all successful runtime samples render',
    );
    await evaluate(`document.querySelector('.summary mat-select').click()`);
    await waitFor(`document.querySelectorAll('mat-option').length === 2`);
    await evaluate(
      `Array.from(document.querySelectorAll('mat-option')).find(el => el.textContent.includes('Quality')).click()`,
    );
    await waitFor(
      `document.querySelector('.summary mat-select').textContent.includes('Quality') && Array.from(document.querySelectorAll('.highcharts-axis-title')).some(el => el.textContent.includes('Quality'))`,
    );
    assert.ok(requests.some((url) => url.searchParams.get('objective') === 'Quality'));
    await clickText('.browser tbody button', 'Instance 11');
    await waitFor(`document.querySelector('.detail-title').textContent.includes('Instance 11')`);
    await clickText('[role=tab]', 'Results');
    await waitFor(`document.body.textContent.includes('Repetition result ledger')`);
    await evaluate(
      `Array.from(document.querySelectorAll('mat-select')).find(el => el.closest('mat-form-field').textContent.includes('Outcome')).click()`,
    );
    await waitFor(
      `Array.from(document.querySelectorAll('mat-option')).some(el => el.textContent.trim() === 'failed')`,
    );
    await clickText('mat-option', 'failed');
    await waitFor(`document.querySelectorAll('.browser tbody tr').length === 12`);
    await evaluate(`document.querySelector('.browser tbody button').click()`);
    await waitFor(
      `document.querySelector('app-solution-renderer').textContent.includes('Fixture computation failed')`,
    );
    await clickText('.result-heading button', 'Close');
    await clickText('[role=tab]', 'Issues');
    await waitFor(`document.querySelector('.issue')`);
    await clickText('button', 'Mark issues seen');
    await waitFor(`!document.body.textContent.includes('unseen issues')`);
    await clickText('[role=tab]', 'Artifacts');
    await waitFor(`document.querySelector('a[href*="artifact-1"]')`);
    for (const socket of sockets) socket.destroy();
    await waitFor(`document.querySelector('mat-chip').textContent.trim() === 'Live'`);
    unavailable = true;
    await clickText('[role=tab]', 'Diagnostics');
    await clickText('button', 'Reconnect / refresh');
    await waitFor(`document.querySelector('[role=alert]')`);
    unavailable = false;
    await clickText('button', 'Retry now');
    await waitFor(`!document.querySelector('[role=alert]')`);
    status.revision++;
    status.phase = 'COMPLETED_WITH_ERRORS';
    status.counts = {
      planned: 72,
      pending: 0,
      running: 0,
      succeeded: 60,
      failed: 12,
      cancelled: 0,
    };
    status.completedInstances = 12;
    status.completedAlgorithms = 24;
    status.runningInstances = 0;
    status.runningAlgorithms = 0;
    notify('CustomEvent');
    await waitFor(
      `document.querySelector('.phase').textContent.toLowerCase().includes('completed with errors')`,
    );
    await clickText('[role=tab]', 'Overview');
    await waitFor(`document.querySelectorAll('.highcharts-root').length === 4`);
  } else {
    const statusResponse = await (await fetch(address + '/api/experiments/status')).json();
    assert.ok(statusResponse.runId);
    assert.ok(statusResponse.counts.succeeded > 0);
    const files = await (
      await fetch(address + `/api/experiments/artifacts?runId=${statusResponse.runId}`)
    ).json();
    assert.ok(files.artifacts.length > 0, 'real backend registered exported artifacts');
    for (const artifact of files.artifacts)
      assert.equal(
        (
          await fetch(
            address + `/api/experiments/artifacts/${artifact.id}?runId=${statusResponse.runId}`,
          )
        ).status,
        200,
      );
    await clickText('[role=tab]', 'Results');
    await waitFor(`document.querySelector('.browser tbody button')`);
    await evaluate(`document.querySelector('.browser tbody button').click()`);
    await waitFor(`document.querySelector('app-solution-renderer')`);
    await clickText('.result-heading button', 'Close');
    await clickText('[role=tab]', 'Overview');
    const html = await (await fetch(address)).text();
    for (const match of html.matchAll(/(?:src|href)="([^"]+\.(?:js|css|svg))"/g))
      assert.equal((await fetch(new URL(match[1], address))).status, 200);
  }
  await send('Emulation.setDeviceMetricsOverride', {
    width: 390,
    height: 844,
    deviceScaleFactor: 1,
    mobile: true,
  });
  await waitFor(`document.querySelectorAll('.highcharts-root').length === 4`);
  await waitFor(
    `Array.from(document.querySelectorAll('highcharts-chart')).every(el => Math.abs(el.clientWidth - el.querySelector('.highcharts-root').getBoundingClientRect().width) < 2)`,
  );
  assert.equal(
    await evaluate('document.documentElement.scrollWidth <= window.innerWidth + 1'),
    true,
    'mobile page must not overflow',
  );
  await writeFile(
    join(artifacts, 'mobile.png'),
    Buffer.from(
      (await send('Page.captureScreenshot', { captureBeyondViewport: true })).data,
      'base64',
    ),
  );
  assert.deepEqual(errors, [], 'browser runtime errors');
  console.log(
    JSON.stringify({
      passed: true,
      backend: server ? 'fixture' : 'real',
      charts: 4,
      progressBars: 3,
      mobile: true,
      artifacts,
    }),
  );
} catch (error) {
  console.error(`Browser artifacts: ${artifacts}`);
  throw error;
} finally {
  ws?.close();
  browser.kill();
  for (const socket of sockets) socket.destroy();
  server?.closeAllConnections();
  server?.close();
}
