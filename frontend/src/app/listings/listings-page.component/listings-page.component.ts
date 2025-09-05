import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';

import { ListingsApi } from '../api/listings-api';
import { CategoryItem, ListingSummary, PageResponse } from '../api/models';

@Component({
  selector: 'app-listings-page',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe],
  templateUrl: './listings-page.component.html',
  styleUrls: ['./listings-page.component.scss'],
})
export class ListingsPageComponent implements OnInit {
  // UI States, what my component remembers about itself
  q = '';
  sort: 'createdAt' | 'priceSats' = 'createdAt';
  order: 'ASC' | 'DESC' = 'ASC';
  page = 0;
  size = 10;

  // This is the current category path in the URL, depending on how deep the user gets into the category tree it changes, therefore also my api call to the backend changes
  catPath = '';   // '' means top-level, ALL categories

  // Dependency Injection like on beans in Spring, 
  private readonly api = inject(ListingsApi);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  //Observables which I can use in the HTML with async
  listings$!: Observable<PageResponse<ListingSummary>>;   //all listings
  cats$!: Observable<CategoryItem[]>;                     // all categories

  // ---------- Lifecycle onInit ----------
  ngOnInit(): void {
    const params = this.route.snapshot.queryParamMap;

    this.q = params.get('q') ?? '';
    this.catPath = params.get('cat') ?? '';
    this.sort = (params.get('sort') as 'createdAt' | 'priceSats') ?? 'createdAt';
    this.order = (params.get('order') as 'ASC' | 'DESC') ?? 'ASC';
    this.page = Number(params.get('page') ?? 0);
    this.size = Number(params.get('size') ?? 10);

    this.reload();
  }


  // URL syncing, if null then its not mentioned in the URL
  private syncUrl(): void {
    const queryParams: any = {
      q: this.q.trim() || null,
      cat: this.catPath || null, 
      sort: this.sort,
      order: this.order,
      page: this.page,
      size: this.size,
    };

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams,
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
  }

  reload(resetPage = false): void {
    if (resetPage) this.page = 0;

    this.syncUrl();

    // gets me all the listings depending on the current states
    this.listings$ = this.api.list(
      this.q.trim(),
      this.catPath,
      this.sort,
      this.order,
      this.page,
      this.size
    );

    // gets me all the categories for navigation and display on the UI
    this.cats$ = this.api.getAllCategories(this.catPath);
  }

  // ---------- Breadcrumbs ----------
  
  //returns an array with all the segments of the category path
  crumbs(): string[] {
    return this.catPath?.split("/") ?? [];
  }

  goToLevel(index: number): void {
    if (index < 0) {
      this.catPath = '';       // All categories
    } else {
      // joins all the segments to a string
      this.catPath = this.crumbs().slice(0, index + 1).join('/');
    }
    this.reload(true);
  }


  // returns me a pretty version of the segment for the UI
  pretty(seg: string): string {
    return seg
      .split('-')
      .filter(Boolean)
      .map(s => s.charAt(0).toUpperCase() + s.slice(1))
      .join(' ');
  }

  // ---------- UI actions ----------

  // go to the ALL categories view
  setTop(): void {
    this.catPath = '';
    this.reload(true);
  }

  // when a child of a category is clicked, you go into it and get all its children listed + all listings in that category
  goInto(child: CategoryItem): void {
    this.catPath = child.path;
    this.reload(true);
  }

  // on search and order, I think it makes sense to reset the page to 0
  searchOrSort(): void {
    this.reload(true);
  }

  toggleOrder(): void {
    this.order = this.order === 'ASC' ? 'DESC' : 'ASC';
    this.reload(true);
  }

  next(): void {
    this.page += 1;
    this.reload();
    window.scrollTo({ top: 0 });
  }

  previous(): void {
    this.page = Math.max(0, this.page - 1);
    this.reload();
    window.scrollTo({ top: 0 });
  }

  navigateToListingDetail(id: string): void {
    this.router.navigate(['/listings', id]);
  }

  createListing(): void {
    this.router.navigate(['/listings/new']);
  }
}
