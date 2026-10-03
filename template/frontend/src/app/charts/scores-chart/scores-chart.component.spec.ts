import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScoresChartComponent } from './scores-chart.component';
import {ChartComponent} from "../base-chart/chart.component";

describe('ScoresChartComponent', () => {
  let component: ScoresChartComponent;
  let fixture: ComponentFixture<ScoresChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ScoresChartComponent, ChartComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScoresChartComponent);
    component = fixture.componentInstance;
    component.config = {instance_name: 'test-instance'};
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
