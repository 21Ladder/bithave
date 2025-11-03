import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AddCartItemRequest, Cart, UpdateCartItemRequest } from './models';

@Injectable({ providedIn: 'root' })
export class CartApi {
  constructor(private http: HttpClient) {}

  createCart(): Observable<Cart> {
    return this.http.post<Cart>('/api/v1/carts', {});
  }

  getCart(cartId: string): Observable<Cart> {
    return this.http.get<Cart>(`/api/v1/carts/${cartId}`);
  }

  addItem(cartId: string, request: AddCartItemRequest): Observable<Cart> {
    return this.http.post<Cart>(`/api/v1/carts/${cartId}/items`, request);
  }

  updateItem(cartId: string, itemId: string, request: UpdateCartItemRequest): Observable<Cart> {
    return this.http.put<Cart>(`/api/v1/carts/${cartId}/items/${itemId}`, request);
  }

  removeItem(cartId: string, itemId: string): Observable<Cart> {
    return this.http.delete<Cart>(`/api/v1/carts/${cartId}/items/${itemId}`);
  }
}
