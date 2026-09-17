import { Routes } from '@angular/router';
import { adminGuard, authGuard } from './auth/auth.guards';

// Every route is lazy, so the initial bundle stays small
export const routes: Routes = [
  {
    path: 'listings',
    title: 'Shop | Bithave',
    loadComponent: () =>
      import('./listings/listings-page.component/listings-page.component').then(
        (m) => m.ListingsPageComponent
      ),
  },
  {
    path: 'listings/new',
    title: 'New listing | Bithave',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./listings/listing-create.component/listing-create.component').then(
        (m) => m.ListingCreateComponent
      ),
  },
  {
    path: 'listings/:id/edit',
    title: 'Edit listing | Bithave',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./listings/listing-edit.component/listing-edit.component').then(
        (m) => m.ListingEditComponent
      ),
  },
  {
    path: 'listings/:id',
    title: 'Product | Bithave',
    loadComponent: () =>
      import('./listings/listing-detail.component/listing-detail.component').then(
        (m) => m.ListingDetailComponent
      ),
  },
  {
    path: 'cart',
    title: 'Cart | Bithave',
    loadComponent: () =>
      import('./cart/cart-page.component/cart-page.component').then((m) => m.CartPageComponent),
  },
  {
    path: 'checkout',
    title: 'Checkout | Bithave',
    loadComponent: () =>
      import('./checkout/checkout-page.component').then((m) => m.CheckoutPageComponent),
  },
  {
    path: 'orders/:orderNumber',
    title: 'Order | Bithave',
    loadComponent: () =>
      import('./orders/order-status.component').then((m) => m.OrderStatusComponent),
  },
  {
    path: 'account/orders',
    title: 'Your orders | Bithave',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./orders/order-history.component').then((m) => m.OrderHistoryComponent),
  },
  {
    path: 'login',
    title: 'Sign in | Bithave',
    loadComponent: () => import('./auth/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    title: 'Create account | Bithave',
    loadComponent: () => import('./auth/register.component').then((m) => m.RegisterComponent),
  },
  { path: '', pathMatch: 'full', redirectTo: 'listings' },
  {
    path: '**',
    title: 'Not found | Bithave',
    loadComponent: () => import('./core/not-found.component').then((m) => m.NotFoundComponent),
  },
];
