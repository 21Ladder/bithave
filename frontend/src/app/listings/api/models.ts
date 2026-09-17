export type ListingStatus = 'ACTIVE' | 'SOLD' | 'ARCHIVED';

export interface ListingSummary {
  id: string;
  title: string;
  priceCents: number;
  categoryPath: string;
  status: ListingStatus;
  thumbnailUrl: string | null;
  createdAt: string;
  availableQuantity: number;
}

export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
}

export interface ListingDetail {
  id: string;
  title: string;
  priceCents: number;
  categoryPath: string;
  status: ListingStatus;
  images: string[] | null;
  sellerId: string;
  createdAt: string;
  updatedAt: string;
  quantity: number;
  reservedQuantity: number;
  availableQuantity: number;
}

// sellerId comes from the signed in operator, the client never sends it
export interface CreateListingRequest {
  title: string;
  priceCents: number;
  quantity: number;
  images: string[] | null;
  categoryPath: string;
}

export interface EditListingRequest {
  title?: string;
  priceCents?: number;
  quantity?: number;
  status?: string;
  images?: string[];
}

export interface CategoryItem {
  path: string;
  name: string;
  hasChildren: boolean;
}

export interface BtcPrice {
  priceEur: number;
  updatedAt: string | null;
}
