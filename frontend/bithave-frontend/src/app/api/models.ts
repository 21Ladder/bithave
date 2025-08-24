export interface ListingSummary{
  id: string;
  title: string;
  priceSats: number;
  status: 'ACTIVE'|'SOLD'|'ARCHIVED'
  thumbnailUrl: string|null;
  createdAt: string;
}

export interface PageResponse<T> {
  items: T[];
  page: number; size: number;
  totalElements: number; totalPages: number;
  hasNext: boolean;
}