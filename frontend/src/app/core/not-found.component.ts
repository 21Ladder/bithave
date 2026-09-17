import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page empty">
      <h1>Page not found</h1>
      <p class="text-muted">The page you were looking for does not exist.</p>
      <a class="btn btn--primary" routerLink="/listings">Back to the shop</a>
    </div>
  `,
  styles: [
    `
      .empty {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: var(--space-4);
        padding-block: var(--space-10);
        text-align: center;
      }
    `,
  ],
})
export class NotFoundComponent {}
