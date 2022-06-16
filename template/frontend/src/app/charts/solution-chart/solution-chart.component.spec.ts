import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SolutionChartComponent } from './solution-chart.component';

describe('SolutionChartComponent', () => {
  let component: SolutionChartComponent;
  let fixture: ComponentFixture<SolutionChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SolutionChartComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SolutionChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
