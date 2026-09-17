export type OrderStatus = 'PENDING_PAYMENT' | 'PAID' | 'FULFILLED' | 'EXPIRED' | 'CANCELLED';

export interface OrderItem {
  listingId: string;
  title: string;
  unitPriceCents: number;
  quantity: number;
  subtotalCents: number;
}

export interface Order {
  orderNumber: string;
  status: OrderStatus;
  email: string;
  items: OrderItem[];
  totalCents: number;
  currency: string;
  checkoutLink: string | null;
  createdAt: string;
  paidAt: string | null;
}
