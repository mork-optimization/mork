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
  unavailable = false,
  autollmMode = false,
  comparisonPoll = 0;
const recoveryMutations = [];
const comparisonPaths = [0, 1].map(index => `/home/ubuntu/mork-autollm/campaigns/comparison-fixture/data/comparison/0000000${index}/n250w200.00${index + 1}.txt`);
const fixtureLogLines = Array.from({ length: 10000 }, (_, index) => `Log line ${index + 1}: experiment output`);
const fixtureLog = fixtureLogLines.join('\n') + '\n';
const comparisonConfig = {
  project: { repository: '/fixture', ref: 'fixed', buildCommand: ['./mvnw', 'verify'], artifact: 'project.jar', runArguments: [] },
  workspace: '/fixture/work', trials: 1, seed: 1234, maxParameters: 299, sotaFile: '/sota.json', initialConfigurationsFile: null, trainingPath: '/train.index', comparisonPath: '/test.index',
  codex: { executable: 'codex', maxConcurrentSessions: 1 }, preparation: { pilotDurationMillis: 1000, maxRepairs: 1 },
  tuning: { evaluations: 10000, ignoreInitialMillis: 10000, intervalDurationMillis: 50000, logScaleArea: true },
  comparison: { repetitions: 30, timeLimitMillis: 10000 }, execution: { target: 'LOCAL', workers: 16, heap: '2g' },
};
const comparisonCampaign = {
  id: 'comparison-fixture', status: 'RUNNING', createdAt: new Date().toISOString(), config: comparisonConfig, versions: {}, poolRevision: 1, targets: {},
  trials: [{ number: 1, status: 'COMPARISON', tuningSeed: 1234, repairTurns: 0, attempts: [] }],
};
const comparisonCurves = [];
for (const [instanceIndex, instance] of comparisonPaths.entries()) {
  for (const [algorithmIndex, algorithm] of ['SOTA', 'IraceProposal'].entries()) {
    for (let iteration = 0; iteration < 30; iteration++) {
      const final = 865 + iteration * .03 + algorithmIndex * .07 + instanceIndex * 40;
      comparisonCurves.push({ instance, instanceId: `instance-${instanceIndex}`, algorithm, iteration: String(iteration), curves: {
        Cost: [{ instant: 0, value: 940 + iteration }, { instant: 20e6, value: 900 + iteration * .1 }, { instant: 100e6, value: 880 + iteration * .1 }, { instant: 600e6, value: final + 2 }, { instant: 1500e6, value: final + .1 }, { instant: 2200e6, value: final }],
        Signed: [{ instant: 0, value: 0 }, { instant: 100e6, value: -1 - iteration }],
      } });
    }
  }
}
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
      body = { mode: autollmMode ? 'AUTOLLM' : 'STANDARD', eventLogId: 'browser-log' };
    else if (autollmMode && url.pathname.startsWith('/api/autollm/')) {
      const path = url.pathname.slice('/api/autollm/'.length);
      if (path === 'defaults') body = comparisonConfig;
      else if (path === 'campaigns') body = { active: comparisonCampaign.id, campaigns: [comparisonCampaign] };
      else if (path === 'screenings') body = { active: '', jobs: [] };
      else if (path.endsWith('/recovery')) {
        const action = allowed => ({ allowed, reason: allowed ? null : 'Work is already active', effect: 'Preserve saved evidence and checkpoints' });
        body = { resumeCampaign: action(false), targets: {}, trials: Object.fromEntries(comparisonCampaign.trials.map(trial => [trial.number, { resume: action(trial.status === 'BLOCKED'), fail: action(trial.status === 'BLOCKED') }])) };
      } else if (request.method === 'POST' && /\/trials\/2\/(fail|resume)$/.test(path)) {
        let payload = ''; for await (const chunk of request) payload += chunk;
        recoveryMutations.push({ path, body: JSON.parse(payload || '{}') });
        comparisonCampaign.trials[1].status = path.endsWith('/fail') ? 'FAILED' : 'STARTING';
        body = comparisonCampaign;
      }
      else if (path.endsWith('/progress')) body = { status: 'COMPARISON', infrastructureRetries: 0, repairTurns: 0 };
      else if (path.endsWith('/comparison')) body = { counts: { SOTA: { SUCCEEDED: 60 }, IraceProposal: { SUCCEEDED: 60 } }, pollCount: ++comparisonPoll, curves: comparisonCurves };
      else if (path.endsWith('/artifacts')) body = [{ path: 'attempt/stdout.log', size: Buffer.byteLength(fixtureLog), sha256: 'fixture' }];
      else if (path.endsWith('/preview')) {
        const lines = Number(url.searchParams.get('lines'));
        body = { text: fixtureLogLines.slice(-lines).join('\n') + '\n', lineCount: lines, sizeBytes: Buffer.byteLength(fixtureLog), truncated: true, byteLimited: false };
      } else if (path.endsWith('/artifact')) {
        response.writeHead(200, { 'Content-Type': 'application/octet-stream', 'Content-Disposition': 'attachment; filename="stdout.log"' }).end(fixtureLog);
        return;
      }
      else body = [];
    }
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
    await waitFor(`document.querySelectorAll('.reference-line').length === 4`);
    assert.ok(
      await evaluate(
        `Array.from(document.querySelectorAll('.reference-line')).every(line => line.getAttribute('stroke') === '#d32f2f' && line.getBoundingClientRect().width > 1 && getComputedStyle(line).visibility !== 'hidden')`,
      ),
      'all four charts render the red reference outside the observed scores',
    );
    const referenceToggle = '.reference-toggle input[type=checkbox]';
    await evaluate(`document.querySelector(${JSON.stringify(referenceToggle)}).click()`);
    await waitFor(`document.querySelectorAll('.reference-line').length === 0`);
    await evaluate(`document.querySelector('.summary mat-select').click()`);
    await waitFor(`document.querySelectorAll('mat-option').length === 2`);
    await evaluate(
      `Array.from(document.querySelectorAll('mat-option')).find(el => el.textContent.includes('Quality')).click()`,
    );
    await waitFor(
      `document.querySelector('.summary mat-select').textContent.includes('Quality') && Array.from(document.querySelectorAll('.highcharts-axis-title')).some(el => el.textContent.includes('Quality'))`,
    );
    assert.ok(requests.some((url) => url.searchParams.get('objective') === 'Quality'));
    await waitFor(
      `document.querySelector('.reference-toggle').textContent.includes('Quality: 10')`,
    );
    assert.equal(
      await evaluate(`document.querySelector(${JSON.stringify(referenceToggle)}).checked`),
      false,
      'reference visibility survives objective changes',
    );
    assert.equal(await evaluate(`document.querySelectorAll('.reference-line').length`), 0);
    await evaluate(`document.querySelector(${JSON.stringify(referenceToggle)}).click()`);
    await waitFor(`document.querySelectorAll('.reference-line').length === 4`);
    assert.ok(
      await evaluate(
        `Array.from(document.querySelectorAll('.reference-line')).every(line => line.getBoundingClientRect().width > 1 && getComputedStyle(line).visibility !== 'hidden')`,
      ),
      'the maximization reference is visible above all observed scores',
    );
    await clickText('.browser tbody button', 'Instance 11');
    await waitFor(`document.querySelector('.detail-title').textContent.includes('Instance 11')`);
    await waitFor(`document.querySelectorAll('.reference-line').length === 4`);
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
  if (server) {
    autollmMode = true;
    await send('Emulation.setDeviceMetricsOverride', { width: 1440, height: 1100, deviceScaleFactor: 1, mobile: false });
    await send('Page.navigate', { url: address });
    await waitFor(`document.querySelector('.comparison-controls') && document.querySelectorAll('.highcharts-series-group .highcharts-graph').length === 60`);
    const controls = `document.querySelectorAll('.comparison-controls select')`;
    assert.deepEqual(await evaluate(`Array.from(${controls}[0].options).map(option => option.textContent.trim())`), ['00000000/n250w200.001.txt', '00000001/n250w200.002.txt']);
    assert.equal(await evaluate(`${controls}[0].options[1].value`), comparisonPaths[1]);
    assert.equal(await evaluate(`${controls}[0].options[1].title`), comparisonPaths[1]);
    assert.deepEqual(await evaluate(`Array.from(document.querySelectorAll('.highcharts-legend-item text')).map(el => el.textContent)`), ['SOTA', 'IraceProposal']);
    assert.equal(await evaluate(`Array.from(document.querySelectorAll('.highcharts-series-group .highcharts-graph')).every(el => !el.getAttribute('stroke-dasharray') || el.getAttribute('stroke-dasharray') === 'none')`), true, 'all repetitions use solid lines');
    assert.equal(await evaluate(`Array.from(document.querySelectorAll('.highcharts-series-group .highcharts-series.highcharts-line-series')).every(el => Number(el.getAttribute('opacity')) > 0 && Number(el.getAttribute('opacity')) < 1)`), true, 'repetition strokes are translucent');
    assert.equal(await evaluate(`new Set(Array.from(document.querySelectorAll('.highcharts-series-group .highcharts-graph')).map(el => el.getAttribute('stroke'))).size`), 12, 'blue and orange/red shade families');

    await evaluate(`document.querySelector('.highcharts-legend-item').dispatchEvent(new MouseEvent('click', {bubbles: true}))`);
    await waitFor(`Array.from(document.querySelectorAll('.highcharts-series-group .highcharts-series.highcharts-line-series')).filter(el => getComputedStyle(el).visibility !== 'hidden').length === 30`);
    await evaluate(`document.querySelector('.highcharts-legend-item').dispatchEvent(new MouseEvent('click', {bubbles: true}))`);
    await waitFor(`Array.from(document.querySelectorAll('.highcharts-series-group .highcharts-series.highcharts-line-series')).filter(el => getComputedStyle(el).visibility !== 'hidden').length === 60`);

    const chartHeight = await evaluate(`document.querySelector('.highcharts-root').getBoundingClientRect().height`);
    await evaluate(`${controls}[3].value='760'; ${controls}[3].dispatchEvent(new Event('change', {bubbles:true}))`);
    await waitFor(`document.querySelector('.highcharts-root').getBoundingClientRect().height > ${chartHeight + 200}`);
    await evaluate(`document.querySelector('app-dashboard-chart').style.height='900px'`);
    await waitFor(`document.querySelector('.highcharts-root').getBoundingClientRect().height > 880`);
    assert.equal(await evaluate(`getComputedStyle(document.querySelector('app-dashboard-chart')).resize`), 'vertical');

    await evaluate(`document.querySelector('app-dashboard-chart').scrollIntoView({block:'start'})`);
    const plot = await evaluate(`(()=>{const r=document.querySelector('.highcharts-plot-background').getBoundingClientRect();return {x:r.x,y:r.y,width:r.width,height:r.height}})()`);
    await send('Input.dispatchMouseEvent', { type: 'mouseMoved', x: plot.x + plot.width * .2, y: plot.y + plot.height * .2 });
    await send('Input.dispatchMouseEvent', { type: 'mousePressed', button: 'left', buttons: 1, clickCount: 1, x: plot.x + plot.width * .2, y: plot.y + plot.height * .2 });
    await send('Input.dispatchMouseEvent', { type: 'mouseMoved', buttons: 1, x: plot.x + plot.width * .8, y: plot.y + plot.height * .8 });
    await send('Input.dispatchMouseEvent', { type: 'mouseReleased', button: 'left', buttons: 0, clickCount: 1, x: plot.x + plot.width * .8, y: plot.y + plot.height * .8 });
    await waitFor(`document.querySelector('.highcharts-reset-zoom')`);
    const pollAtZoom = comparisonPoll;
    await waitFor(`JSON.parse(Array.from(document.querySelectorAll('details')).find(el => el.querySelector('summary')?.textContent === 'Comparison results').querySelector('pre').textContent).pollCount > ${pollAtZoom}`);
    assert.equal(await evaluate(`!!document.querySelector('.highcharts-reset-zoom')`), true, 'polling preserves the zoom');
    await evaluate(`${controls}[0].value=${JSON.stringify(comparisonPaths[1])}; ${controls}[0].dispatchEvent(new Event('change', {bubbles:true}))`);
    await waitFor(`!document.querySelector('.highcharts-reset-zoom')`);
    await evaluate(`${controls}[2].value='logarithmic'; ${controls}[2].dispatchEvent(new Event('change', {bubbles:true}))`);
    await waitFor(`${controls}[2].value === 'logarithmic' && document.querySelectorAll('.highcharts-series-group .highcharts-graph').length === 60`);
    await evaluate(`${controls}[1].value='Signed'; ${controls}[1].dispatchEvent(new Event('change', {bubbles:true}))`);
    await waitFor(`${controls}[2].value === 'linear' && ${controls}[2].options[1].disabled`);
    await evaluate(`${controls}[1].value='Cost'; ${controls}[1].dispatchEvent(new Event('change', {bubbles:true})); ${controls}[0].value=${JSON.stringify(comparisonPaths[0])}; ${controls}[0].dispatchEvent(new Event('change', {bubbles:true})); ${controls}[2].value='linear'; ${controls}[2].dispatchEvent(new Event('change', {bubbles:true})); ${controls}[3].value='520'; ${controls}[3].dispatchEvent(new Event('change', {bubbles:true}))`);
    await waitFor(`document.querySelector('.highcharts-root').getBoundingClientRect().height < 530`);
    await evaluate(`document.querySelector('.comparison-controls').scrollIntoView({block:'start'})`);
    await writeFile(join(artifacts, 'autollm-comparison.png'), Buffer.from((await send('Page.captureScreenshot')).data, 'base64'));

    await send('Emulation.setDeviceMetricsOverride', { width: 390, height: 844, deviceScaleFactor: 1, mobile: true });
    await waitFor(`Math.abs(document.querySelector('highcharts-chart').clientWidth - document.querySelector('.highcharts-root').getBoundingClientRect().width) < 2`);
    assert.equal(await evaluate('document.documentElement.scrollWidth <= window.innerWidth + 1'), true, 'comparison controls fit a mobile viewport');
    await writeFile(join(artifacts, 'autollm-mobile.png'), Buffer.from((await send('Page.captureScreenshot')).data, 'base64'));

    await send('Emulation.setDeviceMetricsOverride', { width: 1440, height: 1100, deviceScaleFactor: 1, mobile: false });
    await clickText('button', 'Refresh artifacts');
    await waitFor(`document.querySelector('.artifact button')`);
    await clickText('.artifact button', 'Preview tail');
    await waitFor(`document.querySelector('.logs')?.textContent.includes('Log line 10000:')`);
    assert.equal(await evaluate(`document.querySelector('.logs').textContent.split('\\n').filter(Boolean).length`), 500);
    assert.equal(await evaluate(`document.querySelector('.logs').textContent.startsWith('Log line 9501:')`), true);
    await evaluate(`const limit=document.querySelector('.log-preview select'); limit.value='100'; limit.dispatchEvent(new Event('change',{bubbles:true}))`);
    await waitFor(`document.querySelector('.logs')?.textContent.startsWith('Log line 9901:')`);
    assert.equal(await evaluate(`document.querySelector('.logs').textContent.split('\\n').filter(Boolean).length`), 100);
    const downloadUrl = await evaluate(`document.querySelector('.log-preview a[download]').href`);
    const fullLog = await fetch(downloadUrl);
    assert.match(fullLog.headers.get('content-disposition'), /attachment/);
    assert.equal(await fullLog.text(), fixtureLog, 'download contains the full 10,000-line log');
    await evaluate(`document.querySelector('.log-preview').scrollIntoView({block:'start'})`);
    await writeFile(join(artifacts, 'autollm-log-preview.png'), Buffer.from((await send('Page.captureScreenshot')).data, 'base64'));
    await clickText('.log-preview button', 'Close preview');
    await waitFor(`!document.querySelector('.log-preview')`);

    comparisonCampaign.trials.push({ number: 2, status: 'BLOCKED', tuningSeed: 5678, repairTurns: 0, attempts: [] });
    await waitFor(`Array.from(document.querySelectorAll('tr')).some(row=>row.cells[0]?.textContent==='2')`);
    await evaluate(`Array.from(document.querySelectorAll('tr')).find(row=>row.cells[0]?.textContent==='2').querySelector('button').click()`);
    await waitFor(`document.querySelector('.trial-recovery')`);
    assert.equal(await evaluate(`document.querySelectorAll('.trial-recovery button')[1].disabled`), true, 'ending a trial requires an operator reason');
    await evaluate(`const reason=document.querySelector('.trial-recovery textarea');reason.value='Sampling exhausted';reason.dispatchEvent(new Event('input',{bubbles:true}))`);
    await waitFor(`!document.querySelectorAll('.trial-recovery button')[1].disabled`);
    await clickText('.trial-recovery button', 'Mark trial failed and continue');
    await waitFor(`!document.querySelector('.trial-recovery')`);
    assert.deepEqual(recoveryMutations[0], { path: 'campaigns/comparison-fixture/trials/2/fail', body: { reason: 'Sampling exhausted' } });
    comparisonCampaign.trials[1].status = 'BLOCKED';
    await waitFor(`document.querySelector('.trial-recovery')`);
    await clickText('.trial-recovery button', 'Resume existing work');
    await waitFor(`!document.querySelector('.trial-recovery')`);
    assert.equal(recoveryMutations[1].path, 'campaigns/comparison-fixture/trials/2/resume');
  }
  assert.deepEqual(errors, [], 'browser runtime errors');
  console.log(
    JSON.stringify({
      passed: true,
      backend: server ? 'fixture' : 'real',
      charts: 4,
      progressBars: 3,
      mobile: true,
      autollmComparison: Boolean(server),
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
