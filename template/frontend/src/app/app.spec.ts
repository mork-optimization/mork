import { provideZonelessChangeDetection, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { App } from './app';
import { ApplicationView, AutoconfigStore } from './store/autoconfig-store';

class FakeAutoconfigStore {
  readonly viewMode = signal<ApplicationView>('detecting');
  readonly latestError = signal<string | null>(null);
  readonly start = vi.fn();
  readonly retryNow = vi.fn();
}

describe('App mode selection', () => {
  let store: FakeAutoconfigStore;

  beforeEach(async () => {
    store = new FakeAutoconfigStore();
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideZonelessChangeDetection(), { provide: AutoconfigStore, useValue: store }],
    }).compileComponents();
  });

  afterEach(() => TestBed.resetTestingModule());

  it('starts automatic mode detection and renders signal-driven worker state', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    expect(store.start).toHaveBeenCalledOnce();
    expect(fixture.nativeElement.textContent).toContain('Detecting experiment mode');

    store.viewMode.set('worker');
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Autoconfig worker');
    expect(fixture.nativeElement.textContent).toContain('coordinator process');
  });

  it('offers an immediate retry when mode detection is unavailable', async () => {
    store.viewMode.set('unavailable');
    store.latestError.set('Backend request failed (network error)');
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('button') as HTMLButtonElement;
    button.click();
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Backend request failed');
    expect(store.retryNow).toHaveBeenCalledOnce();
  });
});
