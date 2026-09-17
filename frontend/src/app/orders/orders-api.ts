import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/api.config';
import { Order } from './models';

@Injectable({ providedIn: 'root' })
export class OrdersApi {
  private readonly http = inject(HttpClient);
  private readonly base = inject(API_BASE_URL);

  getOrder(orderNumber: string): Observable<Order> {
    return this.http.get<Order>(`${this.base}/orders/${orderNumber}`);
  }

  // History of the signed in account
  getMyOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.base}/account/orders`);
  }
}
