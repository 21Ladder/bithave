import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AsyncPipe, NgIf } from '@angular/common';
import { CartService } from '../cart/cart.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterModule, AsyncPipe, NgIf],
  templateUrl: './app-header.component.html',
  styleUrls: ['./app-header.component.scss'],
})
export class AppHeaderComponent {
  private readonly cartService = inject(CartService);

  readonly cart$ = this.cartService.cart$;
}
