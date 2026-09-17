import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { CartService } from '../cart/cart.service';
import { ThemeService } from '../core/theme.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './app-header.component.html',
  styleUrls: ['./app-header.component.scss'],
})
export class AppHeaderComponent {
  private readonly cartService = inject(CartService);
  private readonly router = inject(Router);
  readonly auth = inject(AuthService);
  readonly theme = inject(ThemeService);

  readonly itemCount = this.cartService.itemCount;

  toggleTheme(): void {
    this.theme.toggle();
  }

  signOut(): void {
    this.auth.logout().subscribe({
      next: () => this.router.navigate(['/listings']),
      error: () => this.router.navigate(['/listings']),
    });
  }
}
