import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/api.config';
import {
  AddCartItemRequest,
  Cart,
  CheckoutRequest,
  CheckoutResponse,
  UpdateCartItemRequest,
} from './models';

@Injectable({ providedIn: 'root' })
export class CartApi {
  private readonly http = inject(HttpClient);
  private readonly base = inject(API_BASE_URL);

  // Create an empty cart
  createCart(): Observable<Cart> {
    return this.http.post<Cart>(`${this.base}/carts`, {});
  }

  // Fetch a cart by id
  getCart(cartId: string): Observable<Cart> {
    return this.http.get<Cart>(`${this.base}/carts/${cartId}`);
  }

  // Add a listing to the cart
  addItem(cartId: string, request: AddCartItemRequest): Observable<Cart> {
    return this.http.post<Cart>(`${this.base}/carts/${cartId}/items`, request);
  }

  // Update quantity of a cart item
  updateItem(cartId: string, itemId: string, request: UpdateCartItemRequest): Observable<Cart> {
    return this.http.put<Cart>(`${this.base}/carts/${cartId}/items/${itemId}`, request);
  }

  // Remove an item from the cart
  removeItem(cartId: string, itemId: string): Observable<Cart> {
    return this.http.delete<Cart>(`${this.base}/carts/${cartId}/items/${itemId}`);
  }

  // Turn the cart into an order and get the BTCPay checkout link
  checkout(request: CheckoutRequest): Observable<CheckoutResponse> {
    return this.http.post<CheckoutResponse>(`${this.base}/checkout`, request);
  }
}
