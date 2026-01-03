import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CartService } from '../cart.service';
import { ListingsApi } from '../../listings/api/listings-api';
import { CartItem } from '../models';

@Component({
  selector: 'app-cart-page',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './cart-page.component.html',
  styleUrls: ['./cart-page.component.scss'],
})
export class CartPageComponent implements OnInit {
  private readonly cartService = inject(CartService);
  private readonly listingsApi = inject(ListingsApi);

  readonly cart$ = this.cartService.cart$;

  loading = true;
  error: string | null = null;
  status: string | null = null;
  workingItemId: string | null = null;
  btcPrice: number | null = null; // USD per BTC
  checkoutError: string | null = null;
  checkoutLoading = false;

  // Lifecycle hook to load cart on component init
  ngOnInit(): void {
    this.cartService.refresh().subscribe({
      next: () => {
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.error = 'Cart could not be loaded.';
      },
    });

    this.listingsApi.getBtcPrice().subscribe({
      next: (price) => (this.btcPrice = price),
      error: () => (this.btcPrice = null),
    });
  }

  // TrackBy function for cart items, improves performance
  trackById(index: number, item: CartItem): string {
    return item.id;
  }

  // Increment item quantity
  increment(item: CartItem): void {
    this.updateQuantity(item, item.quantity + 1);
  }

  // Decrement item quantity
  decrement(item: CartItem): void {
    if (item.quantity <= 1) {
      this.remove(item);
    } else {
      this.updateQuantity(item, item.quantity - 1);
    }
  }

  // Handle direct quantity input, when the user puts a number in the input field
  onQuantityInput(item: CartItem, rawValue: string): void {
    const quantity = Number(rawValue);
    if (!Number.isFinite(quantity) || quantity < 1) {
      this.remove(item);
      return;
    }
    this.updateQuantity(item, Math.floor(quantity));
  }

  // Remove item from cart by button click
  remove(item: CartItem): void {
    this.beginAction(item.id);
    this.cartService.removeItem(item.id).subscribe({
      next: () => this.finishAction('Item removed.'),
      error: () => this.failAction('Item could not be removed.'),
    });
  }

  // Update item quantity helper method used for increment, decrement, and direct input
  private updateQuantity(item: CartItem, quantity: number): void {
    this.beginAction(item.id);
    this.cartService.updateQuantity(item.id, quantity).subscribe({
      next: () => this.finishAction('Quantity updated.'),
      error: () => this.failAction('Quantity could not be updated.'),
    });
  }

  // Helper methods to manage action states like loading, success, and error
  private beginAction(itemId: string): void {
    this.error = null;
    this.status = null;
    this.workingItemId = itemId;
  }

  // mark action as finished successfully to help the user understand what happened
  private finishAction(message: string): void {
    this.workingItemId = null;
    this.status = message;
  }

  // mark action as failed and show an error message
  private failAction(message: string): void {
    this.workingItemId = null;
    this.error = message;
  }

  totalSats(totalUsd: number): number | null {
    if (!this.btcPrice || this.btcPrice <= 0) return null;
    return Math.round((totalUsd / this.btcPrice) * 100_000_000);
  }

  startCheckout(): void {
    this.checkoutError = null;
    this.checkoutLoading = true;
    this.cartService.checkout().subscribe({
      next: (res) => {
        this.checkoutLoading = false;
        if (res.checkoutLink) {
          window.location.href = res.checkoutLink;
        } else {
          this.checkoutError = 'No checkout link returned.';
        }
      },
      error: () => {
        this.checkoutLoading = false;
        this.checkoutError = 'Checkout failed. Please try again.';
      },
    });
  }
}
