import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AddCartItemRequest, Cart, UpdateCartItemRequest, CheckoutResponse } from './models';

@Injectable({ providedIn: 'root' })
export class CartApi {
  constructor(private http: HttpClient) {}

  // Create an empty cart
  createCart(): Observable<Cart> {
    return this.http.post<Cart>('/api/v1/carts', {});
  }

  // Fetch a cart by id
  getCart(cartId: string): Observable<Cart> {
    return this.http.get<Cart>(`/api/v1/carts/${cartId}`);
  }

  // Add a listing to the cart
  addItem(cartId: string, request: AddCartItemRequest): Observable<Cart> {
    return this.http.post<Cart>(`/api/v1/carts/${cartId}/items`, request);
  }

  // Update quantity of a cart item
  updateItem(cartId: string, itemId: string, request: UpdateCartItemRequest): Observable<Cart> {
    return this.http.put<Cart>(`/api/v1/carts/${cartId}/items/${itemId}`, request);
  }

  // Remove an item from the cart
  removeItem(cartId: string, itemId: string): Observable<Cart> {
    return this.http.delete<Cart>(`/api/v1/carts/${cartId}/items/${itemId}`);
  }

  // Start checkout to create a BTCPay invoice (invoice call itself is done in backend, but the frontend provides the cartId and cart total)
  checkout(cartId: string): Observable<CheckoutResponse> {
    return this.http.post<CheckoutResponse>(`/api/v1/carts/${cartId}/checkout`, {});
  }
}
