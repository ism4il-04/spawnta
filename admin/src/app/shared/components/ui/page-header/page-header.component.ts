import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-header">
      <div class="header-info">
        <h1 class="title">{{ title }}</h1>
        <p class="description" *ngIf="description">{{ description }}</p>
      </div>
      <div class="header-actions">
        <ng-content></ng-content>
      </div>
    </div>
  `,
  styles: [`
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
      margin-bottom: 2rem;
    }

    .header-info {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;

      .title {
        font-size: 1.875rem;
        font-weight: 700;
        color: #0f172a;
        margin: 0;
        letter-spacing: -0.025em;
      }

      .description {
        font-size: 1rem;
        color: #64748b;
        margin: 0;
      }
    }

    .header-actions {
      display: flex;
      gap: 0.75rem;
    }
  `]
})
export class PageHeaderComponent {
  @Input({ required: true }) title!: string;
  @Input() description?: string;
}
