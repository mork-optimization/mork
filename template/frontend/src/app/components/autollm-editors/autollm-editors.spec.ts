import { TestBed } from '@angular/core/testing';
import { AutoLlmDatasetEditor } from './dataset-editor';
import { AutoLlmServerEditor } from './server-editor';

describe('AutoLLM structured editors', () => {
  it('preserves off-page dataset membership and requires exclusion reasons', async () => {
    const fixture = TestBed.createComponent(AutoLlmDatasetEditor);
    const testing = Array.from({ length: 60 }, (_, i) => `instance-${i}`);
    fixture.componentRef.setInput('initial', {
      training: ['instance-0'],
      testing,
      excluded: { screened: 'Too easy' },
    });
    await fixture.whenStable();
    const editor = fixture.componentInstance;
    editor.search.set('instance-59');
    expect(editor.visibleRows()).toHaveLength(1);
    editor.update('instance-59', { membership: 'excluded' });
    expect(editor.validation()).toContain('reason');
    editor.update('instance-59', { reason: 'Invalid input' });
    expect(editor.validation()).toBe('');
    expect(editor.selection().testing).toHaveLength(59);
    expect(editor.selection().training).toEqual(['instance-0']);
    expect(editor.selection().excluded).toEqual({
      'instance-59': 'Invalid input',
      screened: 'Too easy',
    });
    editor.update('instance-0', { membership: 'testing' });
    expect(editor.validation()).toContain('at least one');
  });
  it('rejects JSON that drops, duplicates or invents dataset members without changing the form', async () => {
    const fixture = TestBed.createComponent(AutoLlmDatasetEditor);
    const initial = { training: ['a'], testing: ['a', 'b'], excluded: { c: 'Excluded' } };
    fixture.componentRef.setInput('initial', initial);
    await fixture.whenStable();
    const editor = fixture.componentInstance;
    for (const invalid of [
      null,
      { ...initial, testing: ['a'] },
      { ...initial, testing: ['a', 'b', 'd'] },
      { ...initial, training: ['a', 'a'] },
      { ...initial, excluded: { b: 'Overlap', c: 'Excluded' } },
    ]) {
      editor.json.set(JSON.stringify(invalid));
      editor.importJson();
      expect(editor.jsonError()).not.toBe('');
      expect(editor.selection()).toEqual(initial);
    }
    editor.json.set(JSON.stringify({ training: ['b'], testing: ['a', 'b', 'c'], excluded: {} }));
    editor.importJson();
    expect(editor.jsonError()).toBe('');
    expect(editor.selection().training).toEqual(['b']);
    expect(editor.selection().testing).toContain('c');
  });
  it('validates server enrollment and synchronizes advanced JSON with the form', async () => {
    const fixture = TestBed.createComponent(AutoLlmServerEditor);
    fixture.componentRef.setInput('existingIds', ['existing']);
    await fixture.whenStable();
    const editor = fixture.componentInstance;
    expect(editor.validation()).toContain('Enter');
    editor.json.set(
      JSON.stringify({
        id: 'existing',
        host: 'user@host',
        workspace: '/work',
        javaExecutable: 'java',
        environment: { LANG: 'en_US' },
      }),
    );
    editor.importJson();
    expect(editor.validation()).toContain('unique ID');
    editor.update('id', 'new');
    editor.update('workers', 1.5);
    expect(editor.validation()).toContain('whole number');
    editor.update('workers', null);
    editor.addEnvironment();
    editor.updateEnvironment(1, 'key', 'LANG');
    expect(editor.validation()).toContain('unique');
    editor.removeEnvironment(1);
    editor.updateEnvironment(0, 'value', 'C');
    expect(editor.validation()).toBe('');
    expect(editor.value().environment).toEqual({ LANG: 'C' });
    editor.json.set('{"id":3}');
    editor.importJson();
    expect(editor.jsonError()).toContain('string');
    expect(editor.value().id).toBe('new');
  });
});
