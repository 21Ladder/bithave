import { Component, OnInit } from '@angular/core';
import { inject } from '@angular/core';
import { ListingsApi } from './api/listings-api';
import { map, Observable } from 'rxjs';
import { ListingSummary, PageResponse } from './api/models';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';

@Component({
  selector: 'app-listings-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './listings-page.component.html',
  styleUrls: ['./listings-page.component.scss']
})
export class ListingsPageComponent implements OnInit {

  //user search/sort/order input states
  q: string = '';
  sort: 'createdAt' | 'priceSats' = 'createdAt';
  order: 'ASC' | 'DESC' = 'ASC';
  page: number = 0;
  size: number = 10;

  private readonly api = inject(ListingsApi);
  private router = inject(Router);
  private route = inject(ActivatedRoute)
  listingResponse$!: Observable<PageResponse<ListingSummary>>;

  //loads the initial list of all the listings
  ngOnInit() {
    
    //read all the query params from the route
    const params = this.route.snapshot.queryParamMap;
    params.get('q') ? this.q = params.get('q')! : null;
    // gets sort value if possible, should be createdAt or priceSats, null is possible
    params.get('sort') ? this.sort = params.get('sort') as 'createdAt' | 'priceSats' : null;
    params.get('order') ? this.order = params.get('order') as 'ASC' | 'DESC' : null;
    params.get('page') ? this.page = Number(params.get('page')) ?? 0 : null;
    params.get('size') ? this.size = Number(params.get('size')) ?? 10 : null;
    this.reload();
  }

  //search for the listings based on the user input
  searchOrSort(){
    //true because we reset the pagenumber to 0 if search or sort was choosen
    this.reload(true);
  }

  //reloads if the state changed
  reload(resetPage = false){
    if (resetPage) {
      this.page = 0;
    }

    //creating the query params object without the search field, merge handles it the following (if null, q is not set)
    const queryParams: any = {sort: this.sort, order: this.order, page: this.page, size: this.size}
    const trimmedQ = this.q.trim();
    queryParams.q = trimmedQ || null;

    //merges the query params and replaces the url
    this.router.navigate([], {
      relativeTo: this.route, 
      queryParams: queryParams, 
      queryParamsHandling: 'merge', 
      replaceUrl: true
    });
    this.listingResponse$ = this.api.list(trimmedQ, this.sort, this.order, this.page, this.size);
  }

  toggleOrder() {
    this.order = this.order === 'ASC' ? 'DESC' : 'ASC';
    this.reload();
  }

  next(){
    this.page = this.page++;
    this.reload();
    window.scrollTo({top: 0});
  }

  previous(){
    this.page = Math.max(0, this.page--);
    this.reload();
    window.scrollTo({top: 0});
  }
}

  
