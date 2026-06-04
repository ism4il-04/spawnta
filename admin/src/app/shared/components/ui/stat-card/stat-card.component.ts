import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule, LucideIconData, TrendingUp, TrendingDown } from 'lucide-angular';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  template: `
    <div class="stat-card">
      <div class="stat-header">
        <div class="icon-box" [ngStyle]="{'background-color': iconBgColor, 'color': iconColor}">
          <lucide-icon [name]="icon" [size]="20"></lucide-icon>
        </div>
        <div class="trend" *ngIf="trend !== undefined" [class.up]="trend > 0" [class.down]="trend < 0">
          <lucide-icon [name]="trend > 0 ? TrendingUp : TrendingDown" [size]="14"></lucide-icon>
          <span>{{ trend > 0 ? '+' : '' }}{{ trend }}%</span>
        </div>
      </div>
      <div class="stat-content">
        <span class="label">{{ label }}</span>
        <h3 class="value">{{ value }}</h3>
      </div>
    </div>
  `,
  styles: [`
    .stat-card {
      background: white;
      padding: 1.5rem;
      border-radius: 12px;
      border: 1px solid #e5e7eb;
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .stat-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
    }

    .icon-box {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .trend {
      display: flex;
      align-items: center;
      gap: 0.25rem;
      font-size: 0.75rem;
      font-weight: 600;
      padding: 0.25rem 0.5rem;
      border-radius: 20px;

      &.up {
        background: #f0fdf4;
        color: #16a34a;
      }

      &.down {
        background: #fef2f2;
        color: #dc2626;
      }
    }

    .stat-content {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;

      .label {
        font-size: 0.875rem;
        color: #64748b;
        font-weight: 500;
      }

      .value {
        font-size: 1.5rem;
        font-weight: 700;
        color: #0f172a;
        margin: 0;
      }
    }
  `]
})
export class StatCardComponent {
  @Input({ required: true }) label!: string;
  @Input({ required: true }) value!: string | number;
  @Input({ required: true }) icon!: LucideIconData;
  @Input() trend?: number;
  @Input() iconColor = '#3b82f6';
  @Input() iconBgColor = '#eff6ff';

  readonly TrendingUp = TrendingUp;
  readonly TrendingDown = TrendingDown;
}
