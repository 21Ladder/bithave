import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, catchError, map, of, switchMap, tap, throwError } from 'rxjs';
import { ApiError } from '../core/error.interceptor';
import { CartApi } from './cart-api';
import { Cart, CheckoutRequest, CheckoutResponse } from './models';

const STORAGE_KEY = 'bithave-cart-id';

@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly api = inject(CartApi);

  private cartId: string | null = null;
  private readonly cartState = signal<Cart | null>(null);

  readonly cart = this.cartState.asReadonly();
  readonly itemCount = computed(() => this.cartState()?.totalQuantity ?? 0);

  constructor() {
    this.cartId = this.readStoredCartId();

    if (this.cartId) {
      this.refresh().subscribe({ error: () => this.resetCart() });
    }
  }

  addItem(listingId: string, quantity = 1): Observable<Cart> {
    return this.ensureCartId().pipe(
      switchMap((cartId) => this.api.addItem(cartId, { listingId, quantity })),
      tap((cart) => this.storeCart(cart))
    );
  }

  updateQuantity(itemId: string, quantity: number): Observable<Cart> {
    return this.ensureCartId().pipe(
      switchMap((cartId) => this.api.updateItem(cartId, itemId, { quantity })),
      tap((cart) => this.storeCart(cart))
    );
  }

  removeItem(itemId: string): Observable<Cart> {
    return this.ensureCartId().pipe(
      switchMap((cartId) => this.api.removeItem(cartId, itemId)),
      tap((cart) => this.storeCart(cart))
    );
  }

  refresh(): Observable<Cart> {
    if (!this.cartId) {
      return this.createAndStoreCart();
    }

    return this.api.getCart(this.cartId).pipe(
      tap((cart) => this.storeCart(cart)),
      catchError((err: ApiError) => {
        // Gone, expired or owned by another session, start over
        if (err.status === 404 || err.status === 409) {
          return this.createAndStoreCart();
        }
        return throwError(() => err);
      })
    );
  }

  checkout(request: Omit<CheckoutRequest, 'cartId'>): Observable<CheckoutResponse> {
    return this.ensureCartId().pipe(
      switchMap((cartId) => this.api.checkout({ ...request, cartId }))
    );
  }

  // Called once an order is paid, otherwise the buyer returns to an already bought cart
  discardCart(): void {
    this.resetCart();
  }

  private ensureCartId(): Observable<string> {
    if (this.cartId) return of(this.cartId);
    return this.createAndStoreCart().pipe(map((cart) => cart.id));
  }

  private createAndStoreCart(): Observable<Cart> {
    return this.api.createCart().pipe(tap((cart) => this.storeCart(cart)));
  }

  private storeCart(cart: Cart): void {
    this.cartId = cart.id;
    this.cartState.set(cart);
    this.writeCartId(cart.id);
  }

  private resetCart(): void {
    this.cartId = null;
    this.cartState.set(null);
    this.clearStoredCartId();
  }

  private readStoredCartId(): string | null {
    if (typeof window === 'undefined') return null;
    try {
      return window.localStorage.getItem(STORAGE_KEY);
    } catch {
      return null;
    }
  }

  private writeCartId(id: string): void {
    if (typeof window === 'undefined') return;
    try {
      window.localStorage.setItem(STORAGE_KEY, id);
    } catch {
      // ignore storage errors (private mode, etc.)
    }
  }

  private clearStoredCartId(): void {
    if (typeof window === 'undefined') return;
    try {
      window.localStorage.removeItem(STORAGE_KEY);
    } catch {
      // ignore storage errors
    }
  }
}
