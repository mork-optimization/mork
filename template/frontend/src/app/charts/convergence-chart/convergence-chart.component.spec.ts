import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConvergenceChartComponent } from './convergence-chart.component';
import {ChartComponent} from "../base-chart/chart.component";

describe('ConvergenceChartComponent', () => {
  let component: ConvergenceChartComponent;
  let fixture: ComponentFixture<ConvergenceChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ConvergenceChartComponent, ChartComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConvergenceChartComponent);
    component = fixture.componentInstance;
    component.config = {instance_name: 'test-instance'};
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
