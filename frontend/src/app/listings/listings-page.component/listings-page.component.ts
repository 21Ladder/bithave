import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiError } from '../../core/error.interceptor';
import { ListingsApi } from '../api/listings-api';
import { CategoryItem, ListingSummary, PageResponse } from '../api/models';
import { ListingCardComponent } from '../listing-card/listing-card.component';

type SortField = 'createdAt' | 'priceCents';

@Component({
  selector: 'app-listings-page',
  standalone: true,
  imports: [FormsModule, ListingCardComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './listings-page.component.html',
  styleUrls: ['./listings-page.component.scss'],
})
export class ListingsPageComponent implements OnInit {
  // UI state, what this page remembers about itself
  q = '';
  sort: SortField = 'createdAt';
  order: 'ASC' | 'DESC' = 'DESC';
  page = 0;
  size = 12;

  // Current category path in the URL, '' means all categories
  catPath = '';

  private readonly api = inject(ListingsApi);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly result = signal<PageResponse<ListingSummary> | null>(null);
  readonly categories = signal<CategoryItem[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  // Placeholder cards while the request is in flight
  readonly skeletons = Array.from({ length: 8 });

  ngOnInit(): void {
    const params = this.route.snapshot.queryParamMap;

    this.q = params.get('q') ?? '';
    this.catPath = params.get('cat') ?? '';
    this.sort = (params.get('sort') as SortField) ?? 'createdAt';
    this.order = (params.get('order') as 'ASC' | 'DESC') ?? 'DESC';
    this.page = Number(params.get('page') ?? 0);
    this.size = Number(params.get('size') ?? 12);

    this.reload();
  }

  // URL syncing, a null value keeps the key out of the URL
  private syncUrl(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        q: this.q.trim() || null,
        cat: this.catPath || null,
        sort: this.sort,
        order: this.order,
        page: this.page,
        size: this.size,
      },
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
  }

  reload(resetPage = false): void {
    if (resetPage) this.page = 0;

    this.syncUrl();
    this.loading.set(true);
    this.error.set(null);

    this.api
      .list({
        q: this.q.trim(),
        category: this.catPath,
        sort: this.sort,
        order: this.order,
        page: this.page,
        size: this.size,
      })
      .subscribe({
        next: (result) => {
          this.result.set(result);
          this.loading.set(false);
        },
        error: (err: ApiError) => {
          this.loading.set(false);
          this.error.set(err.message);
        },
      });

    this.api.getAllCategories(this.catPath).subscribe({
      next: (cats) => this.categories.set(cats),
      error: () => this.categories.set([]),
    });
  }

  // ---------- Breadcrumbs ----------
  crumbs(): string[] {
    return this.catPath ? this.catPath.split('/') : [];
  }

  goToLevel(index: number): void {
    this.catPath = index < 0 ? '' : this.crumbs().slice(0, index + 1).join('/');
    this.reload(true);
  }

  // Turns a path segment into a readable label
  pretty(seg: string): string {
    return seg
      .split('-')
      .filter(Boolean)
      .map((s) => s.charAt(0).toUpperCase() + s.slice(1))
      .join(' ');
  }

  setTop(): void {
    this.catPath = '';
    this.reload(true);
  }

  goInto(child: CategoryItem): void {
    this.catPath = child.path;
    this.reload(true);
  }

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

  clearFilters(): void {
    this.q = '';
    this.catPath = '';
    this.reload(true);
  }
}
