import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse } from './models';
import { ListingSummary } from './models';

@Injectable({providedIn: 'root'})
export class ListingsApi {
  constructor(private http: HttpClient) {}
  list() {
    return this.http.get<PageResponse<ListingSummary>>('/api/v1/listings');
  }
}
