import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../core/api.config';
import {
  BtcPrice,
  CategoryItem,
  CreateListingRequest,
  EditListingRequest,
  ListingDetail,
  ListingSummary,
  PageResponse,
} from './models';

export interface ListQuery {
  q?: string;
  category?: string;
  sort?: string;
  order?: string;
  page?: number;
  size?: number;
  status?: string;
}

// API service for listings, categories and the BTC price
@Injectable({ providedIn: 'root' })
export class ListingsApi {
  private readonly http = inject(HttpClient);
  private readonly base = inject(API_BASE_URL);

  // List listings with optional filters, sorting and paging
  list(query: ListQuery = {}): Observable<PageResponse<ListingSummary>> {
    let params = new HttpParams()
      .set('page', query.page ?? 0)
      .set('size', query.size ?? 12);

    if (query.status) params = params.set('status', query.status);
    if (query.sort) params = params.set('sort', query.sort);
    if (query.order) params = params.set('order', query.order);
    if (query.q?.trim()) params = params.set('q', query.q.trim());
    if (query.category?.trim()) params = params.set('category', query.category.trim());

    return this.http.get<PageResponse<ListingSummary>>(`${this.base}/listings`, { params });
  }

  // Fetch a single listing by id
  getDetailListing(id: string): Observable<ListingDetail> {
    return this.http.get<ListingDetail>(`${this.base}/listings/${id}`);
  }

  // Admin only
  createListing(newListing: CreateListingRequest): Observable<ListingDetail> {
    return this.http.post<ListingDetail>(`${this.base}/listings`, newListing);
  }

  // Admin only
  editListing(uuid: string, editedListing: EditListingRequest): Observable<ListingDetail> {
    return this.http.patch<ListingDetail>(`${this.base}/listings/${uuid}`, editedListing);
  }

  // Fetch categories below the given parent path
  getAllCategories(parent: string): Observable<CategoryItem[]> {
    return this.http.get<CategoryItem[]>(`${this.base}/categories`, { params: { parent } });
  }

  // Get the latest stored BTC price in Euro
  getBtcPrice(): Observable<BtcPrice> {
    return this.http.get<BtcPrice>(`${this.base}/btcPrice`);
  }
}
