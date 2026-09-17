import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiError } from '../../core/error.interceptor';
import { EurPipe, SatsPipe } from '../../core/money.pipe';
import { ListingsApi } from '../../listings/api/listings-api';
import { CartService } from '../cart.service';
import { CartItem } from '../models';

@Component({
  selector: 'app-cart-page',
  standalone: true,
  imports: [RouterLink, EurPipe, SatsPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './cart-page.component.html',
  styleUrls: ['./cart-page.component.scss'],
})
export class CartPageComponent implements OnInit {
  private readonly cartService = inject(CartService);
  private readonly listingsApi = inject(ListingsApi);

  readonly cart = this.cartService.cart;
  readonly isEmpty = computed(() => (this.cart()?.items.length ?? 0) === 0);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly status = signal<string | null>(null);
  readonly workingItemId = signal<string | null>(null);
  readonly btcPrice = signal<number | null>(null);

  readonly fallback = 'assets/no-image.svg';

  ngOnInit(): void {
    this.cartService.refresh().subscribe({
      next: () => this.loading.set(false),
      error: (err: ApiError) => {
        this.loading.set(false);
        this.error.set(err.message);
      },
    });

    this.listingsApi.getBtcPrice().subscribe({
      next: (price) => this.btcPrice.set(price.priceEur),
      error: () => this.btcPrice.set(null),
    });
  }

  increment(item: CartItem): void {
    this.updateQuantity(item, item.quantity + 1);
  }

  decrement(item: CartItem): void {
    if (item.quantity <= 1) return;
    this.updateQuantity(item, item.quantity - 1);
  }

  onQuantityInput(item: CartItem, rawValue: string): void {
    const quantity = Math.floor(Number(rawValue));
    if (!Number.isFinite(quantity) || quantity < 1) return;
    this.updateQuantity(item, quantity);
  }

  remove(item: CartItem): void {
    this.beginAction(item.id);
    this.cartService.removeItem(item.id).subscribe({
      next: () => this.finishAction('Item removed.'),
      error: (err: ApiError) => this.failAction(err.message),
    });
  }

  onImageError(event: Event): void {
    (event.target as HTMLImageElement).src = this.fallback;
  }

  private updateQuantity(item: CartItem, quantity: number): void {
    this.beginAction(item.id);
    this.cartService.updateQuantity(item.id, quantity).subscribe({
      next: () => this.finishAction('Quantity updated.'),
      error: (err: ApiError) => this.failAction(err.message),
    });
  }

  private beginAction(itemId: string): void {
    this.error.set(null);
    this.status.set(null);
    this.workingItemId.set(itemId);
  }

  private finishAction(message: string): void {
    this.workingItemId.set(null);
    this.status.set(message);
  }

  private failAction(message: string): void {
    this.workingItemId.set(null);
    this.error.set(message);
  }
}
