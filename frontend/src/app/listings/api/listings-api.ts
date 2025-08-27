import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse } from './models';
import { ListingSummary, ListingDetail } from './models';

@Injectable({providedIn: 'root'})
export class ListingsApi {
  constructor(private http: HttpClient) {}

  list(
    q?: string,
    sort?: string,
    order?: string,
    page: number = 0,
    size: number = 10,
    status?: string
  ): Observable<PageResponse<ListingSummary>> {

    const params: any = {page, size};
    if (status) {
      params.status = status;
    }
    if (sort) {
      params.sort = sort;
    }
    if (order) {
      params.order = order;
    }
    if (q && q.trim()){
      params.q = q.trim();
    }
    return this.http.get<PageResponse<ListingSummary>>('/api/v1/listings', {params});
  };

  get(id:string): Observable<ListingDetail> {
    return this.http.get<ListingDetail>(`/api/v1/listings/${id}`);
  }
}

