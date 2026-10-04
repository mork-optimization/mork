import { TestBed } from '@angular/core/testing';
import { PanelPreferences } from './panel-preferences';

describe('PanelPreferences', () => {
  beforeEach(() => localStorage.clear());
  afterEach(() => TestBed.resetTestingModule());

  it('persists hidden and collapsed overview panels and restores all panels', () => {
    const preferences = TestBed.inject(PanelPreferences);
    preferences.hide('current-elites');
    preferences.toggleCollapsed('evaluation-cost');

    TestBed.resetTestingModule();
    const restored = TestBed.inject(PanelPreferences);
    expect(restored.isHidden('current-elites')).toBe(true);
    expect(restored.isCollapsed('evaluation-cost')).toBe(true);

    restored.reset();
    expect(restored.isHidden('current-elites')).toBe(false);
    expect(restored.isCollapsed('evaluation-cost')).toBe(false);
  });
});
