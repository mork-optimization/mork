import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScoresChartComponent } from './scores-chart.component';

describe('ScoresChartComponent', () => {
  let component: ScoresChartComponent;
  let fixture: ComponentFixture<ScoresChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ScoresChartComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScoresChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
