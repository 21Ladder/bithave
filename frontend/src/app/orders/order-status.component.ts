import { ChangeDetectionStrategy, Component, Input, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DestroyRef } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Subscription, timer } from 'rxjs';
import { CartService } from '../cart/cart.service';
import { ApiError } from '../core/error.interceptor';
import { EurPipe } from '../core/money.pipe';
import { OrdersApi } from './orders-api';
import { Order } from './models';

@Component({
  selector: 'app-order-status',
  standalone: true,
  imports: [RouterLink, EurPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './order-status.component.html',
  styleUrls: ['./order-status.component.scss'],
})
export class OrderStatusComponent implements OnInit {
  // Bound from the route via withComponentInputBinding
  @Input() orderNumber = '';

  private readonly api = inject(OrdersApi);
  private readonly cartService = inject(CartService);
  private readonly destroyRef = inject(DestroyRef);

  readonly order = signal<Order | null>(null);
  readonly error = signal<string | null>(null);
  readonly loading = signal(true);

  private pollSubscription?: Subscription;
  private cartCleared = false;

  readonly isPending = computed(() => this.order()?.status === 'PENDING_PAYMENT');
  readonly isPaid = computed(() => {
    const status = this.order()?.status;
    return status === 'PAID' || status === 'FULFILLED';
  });
  readonly isClosed = computed(() => {
    const status = this.order()?.status;
    return status === 'EXPIRED' || status === 'CANCELLED';
  });

  ngOnInit(): void {
    this.load();
    // The webhook settles the order server side, so poll until it lands
    this.pollSubscription = timer(4000, 4000)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        if (this.isPending()) {
          this.load();
        } else {
          this.pollSubscription?.unsubscribe();
        }
      });
  }

  private load(): void {
    this.api.getOrder(this.orderNumber).subscribe({
      next: (order) => {
        this.order.set(order);
        this.loading.set(false);

        // Once paid the cart is spent, otherwise the buyer returns to an already bought cart
        if (!this.cartCleared && (order.status === 'PAID' || order.status === 'FULFILLED')) {
          this.cartCleared = true;
          this.cartService.discardCart();
        }
      },
      error: (err: ApiError) => {
        this.loading.set(false);
        this.error.set(err.message);
      },
    });
  }

  statusLabel(): string {
    switch (this.order()?.status) {
      case 'PENDING_PAYMENT':
        return 'Waiting for payment';
      case 'PAID':
        return 'Paid';
      case 'FULFILLED':
        return 'Shipped';
      case 'EXPIRED':
        return 'Payment window expired';
      case 'CANCELLED':
        return 'Cancelled';
      default:
        return 'Unknown';
    }
  }
}
