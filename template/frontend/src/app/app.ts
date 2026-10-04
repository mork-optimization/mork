import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { StandardDashboard } from './components/standard-dashboard/standard-dashboard';
import { TuningDashboard } from './components/tuning-dashboard/tuning-dashboard';
import { AutoconfigStore } from './store/autoconfig-store';

@Component({
  selector: 'app-root',
  imports: [
    MatButtonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    StandardDashboard,
    TuningDashboard,
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App implements OnInit {
  readonly store = inject(AutoconfigStore);

  ngOnInit(): void {
    this.store.start();
  }
}
