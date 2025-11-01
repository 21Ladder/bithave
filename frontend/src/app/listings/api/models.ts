export interface ListingSummary {
  id: string;
  title: string;
  priceUsd: number;
  status: 'ACTIVE' | 'SOLD' | 'ARCHIVED';
  thumbnailUrl: string | null;
  createdAt: string;
}

export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
}

export interface ListParams {
  q?: string;
  status?: string;
  sort?: string;
  order?: string;
  page?: number;
  size?: number;
}

export interface ListingDetail {
  id: string;
  title: string;
  priceUsd: number;
  status: 'ACTIVE' | 'SOLD' | 'ARCHIVED';
  images: string[] | null;
  sellerId: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateListingRequest {
  title: string;
  priceUsd: number;
  images: string[] | null;
  sellerId: string;
  categoryPath: string;
}

export interface EditListingRequest {
  title?: string;
  priceUsd?: number;
  status?: string;
  images?: string[];
}

export interface CategoryItem {
  path: string;
  name: string;
  hasChildren: boolean;
}

export interface BtcPriceResponse {
  bitcoin: {
    eur: number;
    last_updated_at: number;
  };
}
