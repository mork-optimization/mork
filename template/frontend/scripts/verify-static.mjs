import { access, readFile } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const frontendDirectory = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const staticDirectory = resolve(frontendDirectory, '../src/main/resources/static');
const indexPath = resolve(staticDirectory, 'index.html');
const index = await readFile(indexPath, 'utf8');
const references = [...index.matchAll(/(?:src|href)="([^"]+)"/g)].map((match) => match[1]);
const missing = [];

for (const reference of references) {
  if (/^(?:[a-z]+:|\/\/|#)/i.test(reference)) {
    continue;
  }
  const localPath = reference.split(/[?#]/, 1)[0].replace(/^\//, '');
  if (!localPath) {
    continue;
  }
  try {
    await access(resolve(staticDirectory, localPath));
  } catch {
    missing.push(reference);
  }
}

if (missing.length > 0) {
  throw new Error(`Static index references missing assets: ${missing.join(', ')}`);
}

console.log(`Verified ${references.length} static index references.`);
