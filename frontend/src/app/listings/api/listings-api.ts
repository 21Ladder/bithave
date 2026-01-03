import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CategoryItem, PageResponse } from './models';
import { ListingSummary, ListingDetail, CreateListingRequest, EditListingRequest } from './models';

// API service for Listings, categories, BTC price
@Injectable({ providedIn: 'root' })
export class ListingsApi {
  constructor(private http: HttpClient) {}

  // List listings with optional filters, sorting, and paging
  list(
    q?: string,
    category?: string,
    sort?: string,
    order?: string,
    page: number = 0,
    size: number = 10,
    status?: string
  ): Observable<PageResponse<ListingSummary>> {
    const params: any = { page, size };
    if (status) {
      params.status = status;
    }
    if (sort) {
      params.sort = sort;
    }
    if (order) {
      params.order = order;
    }
    if (q && q.trim()) {
      params.q = q.trim();
    }
    if (category && category.trim()) {
      params.category = category.trim();
    }

    return this.http.get<PageResponse<ListingSummary>>('/api/v1/listings', { params });
  }

  // Fetch a single listing by the id
  getDetailListing(id: string): Observable<ListingDetail> {
    return this.http.get<ListingDetail>(`/api/v1/listings/${id}`);
  }

  // Create a new listing (not needed for user, just for admin/testing)
  createListing(newListing: CreateListingRequest): Observable<ListingDetail> {
    return this.http.post<ListingDetail>('/api/v1/listings', newListing);
  }

  // Edit an existing listing
  editListing(uuid: string, editedListing: EditListingRequest): Observable<ListingDetail> {
    return this.http.patch<ListingDetail>(`/api/v1/listings/${uuid}`, editedListing);
  }

  // Fetch categories (optionally filtered by parent if needed)
  getAllCategories(parent: string): Observable<CategoryItem[]> {
    return this.http.get<CategoryItem[]>('/api/v1/categories', { params: { parent } });
  }

  // Get the latest stored BTC price
  getBtcPrice(): Observable<number> {
    return this.http.get<number>('/api/v1/btcPrice');
  }
}
