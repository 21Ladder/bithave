import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, catchError, map, of, switchMap, tap, throwError } from 'rxjs';
import { CartApi } from './cart-api';
import { Cart } from './models';

const STORAGE_KEY = 'bithave-cart-id';

@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly api = inject(CartApi);

  private cartId: string | null = null;
  private readonly cartSubject = new BehaviorSubject<Cart | null>(null);
  readonly cart$ = this.cartSubject.asObservable();

  constructor() {
    this.cartId = this.readStoredCartId();

    if (this.cartId) {
      this.refresh().subscribe({
        error: () => this.resetCart(),
      });
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
      catchError((err) => {
        if (err.status === 404) {
          return this.createAndStoreCart();
        }
        return throwError(() => err);
      })
    );
  }

  private ensureCartId(): Observable<string> {
    if (this.cartId) {
      return of(this.cartId);
    }
    return this.createAndStoreCart().pipe(map((cart) => cart.id));
  }

  private createAndStoreCart(): Observable<Cart> {
    return this.api.createCart().pipe(tap((cart) => this.storeCart(cart)));
  }

  private storeCart(cart: Cart): void {
    this.cartId = cart.id;
    this.cartSubject.next(cart);
    this.writeCartId(cart.id);
  }

  private resetCart(): void {
    this.cartId = null;
    this.cartSubject.next(null);
    this.clearStoredCartId();
  }

  private readStoredCartId(): string | null {
    if (typeof window === 'undefined') {
      return null;
    }
    try {
      return window.localStorage.getItem(STORAGE_KEY);
    } catch {
      return null;
    }
  }

  private writeCartId(id: string): void {
    if (typeof window === 'undefined') {
      return;
    }
    try {
      window.localStorage.setItem(STORAGE_KEY, id);
    } catch {
      // ignore storage errors (private mode, etc.)
    }
  }

  private clearStoredCartId(): void {
    if (typeof window === 'undefined') {
      return;
    }
    try {
      window.localStorage.removeItem(STORAGE_KEY);
    } catch {
      // ignore storage errors
    }
  }
}
