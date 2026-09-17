import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CartService } from '../cart/cart.service';
import { ApiError } from '../core/error.interceptor';
import { EurPipe } from '../core/money.pipe';

@Component({
  selector: 'app-checkout-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, EurPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './checkout-page.component.html',
  styleUrls: ['./checkout-page.component.scss'],
})
export class CheckoutPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly cartService = inject(CartService);
  private readonly router = inject(Router);

  readonly cart = this.cartService.cart;
  readonly isEmpty = computed(() => (this.cart()?.items.length ?? 0) === 0);

  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    name: ['', [Validators.required]],
    street: ['', [Validators.required]],
    postalCode: ['', [Validators.required]],
    city: ['', [Validators.required]],
    country: ['AT', [Validators.required, Validators.pattern(/^[A-Za-z]{2}$/)]],
  });

  constructor() {
    this.cartService.refresh().subscribe({ error: () => undefined });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.error.set(null);
    const value = this.form.getRawValue();

    this.cartService
      .checkout({
        email: value.email,
        shippingAddress: {
          name: value.name,
          street: value.street,
          postalCode: value.postalCode,
          city: value.city,
          country: value.country.toUpperCase(),
        },
      })
      .subscribe({
        next: (response) => {
          if (response.checkoutLink) {
            // Leave the SPA for BTCPay, which sends the buyer back to /orders/:orderNumber
            window.location.href = response.checkoutLink;
            return;
          }
          this.router.navigate(['/orders', response.orderNumber]);
        },
        error: (err: ApiError) => {
          this.submitting.set(false);
          this.error.set(err.message);
        },
      });
  }
}
