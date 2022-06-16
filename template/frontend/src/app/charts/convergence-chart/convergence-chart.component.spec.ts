import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConvergenceChartComponent } from './convergence-chart.component';

describe('ConvergenceChartComponent', () => {
  let component: ConvergenceChartComponent;
  let fixture: ComponentFixture<ConvergenceChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ConvergenceChartComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConvergenceChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
