export interface CartItem {
  id: string;
  listingId: string;
  title: string;
  thumbnailUrl: string | null;
  unitPriceUsd: number;
  quantity: number;
  subtotalUsd: number;
}

export interface Cart {
  id: string;
  items: CartItem[];
  totalQuantity: number;
  totalPriceUsd: number;
}

export interface AddCartItemRequest {
  listingId: string;
  quantity?: number;
}

export interface UpdateCartItemRequest {
  quantity: number;
}
