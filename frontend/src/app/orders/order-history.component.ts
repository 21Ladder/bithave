import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiError } from '../core/error.interceptor';
import { EurPipe } from '../core/money.pipe';
import { Order } from './models';
import { OrdersApi } from './orders-api';

@Component({
  selector: 'app-order-history',
  standalone: true,
  imports: [RouterLink, DatePipe, EurPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './order-history.component.html',
  styleUrls: ['./order-history.component.scss'],
})
export class OrderHistoryComponent implements OnInit {
  private readonly api = inject(OrdersApi);

  readonly orders = signal<Order[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.api.getMyOrders().subscribe({
      next: (orders) => {
        this.orders.set(orders);
        this.loading.set(false);
      },
      error: (err: ApiError) => {
        this.loading.set(false);
        this.error.set(err.message);
      },
    });
  }

  statusLabel(status: string): string {
    switch (status) {
      case 'PENDING_PAYMENT':
        return 'Waiting for payment';
      case 'PAID':
        return 'Paid';
      case 'FULFILLED':
        return 'Shipped';
      case 'EXPIRED':
        return 'Expired';
      case 'CANCELLED':
        return 'Cancelled';
      default:
        return status;
    }
  }

  statusClass(status: string): string {
    if (status === 'PAID' || status === 'FULFILLED') return 'badge--success';
    if (status === 'PENDING_PAYMENT') return 'badge--warning';
    return 'badge--danger';
  }
}
