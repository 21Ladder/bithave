import { Component, OnInit } from '@angular/core';
import { inject } from '@angular/core';
import { ListingsApi } from './api/listings-api';
import { map, Observable } from 'rxjs';
import { ListingSummary, PageResponse } from './api/models';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-listings-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './listings-page.component.html',
  styleUrl: './listings-page.component.scss'
})
export class ListingsPageComponent implements OnInit {

  //user search/sort/order input states
  q: string = '';
  sort: 'createdAt' | 'priceSats' = 'createdAt';
  order: 'ASC' | 'DESC' = 'ASC';
  page: number = 0;
  size: number = 10;

  private readonly api = inject(ListingsApi);
  listingResponse$!: Observable<PageResponse<ListingSummary>>;

  //loads the initial list of all the listings
  ngOnInit() {
      this.listingResponse$ = this.api.list();
  }

  //search for the listings based on the user input
  searchOrSort(){
    this.page = 0;
    this.reload();
  }

  //reloads if state changed
  reload(){
    this.listingResponse$ = this.api.list(this.q, this.sort, this.order, this.page, this.size);
  }

  toggleOrder() {
    this.order = this.order === 'ASC' ? 'DESC' : 'ASC';
    this.reload();
  }

  next(){
    this.page = this.page++;
    window.scrollTo({top: 0});
    this.reload();
  }

  previous(){
    this.page = Math.max(0, this.page--);
    this.reload();
    window.scrollTo({top: 0});
  }
}

  
