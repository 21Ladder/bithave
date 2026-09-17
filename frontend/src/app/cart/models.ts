export interface CartItem {
  id: string;
  listingId: string;
  title: string;
  thumbnailUrl: string | null;
  unitPriceCents: number;
  quantity: number;
  subtotalCents: number;
}

export interface Cart {
  id: string;
  items: CartItem[];
  totalQuantity: number;
  totalPriceCents: number;
}

export interface AddCartItemRequest {
  listingId: string;
  quantity?: number;
}

export interface UpdateCartItemRequest {
  quantity: number;
}

export interface ShippingAddress {
  name: string;
  street: string;
  postalCode: string;
  city: string;
  country: string;
}

export interface CheckoutRequest {
  cartId: string;
  email: string;
  shippingAddress: ShippingAddress;
}

export interface CheckoutResponse {
  orderNumber: string;
  status: string;
  checkoutLink: string | null;
  totalCents: number;
  currency: string;
}
