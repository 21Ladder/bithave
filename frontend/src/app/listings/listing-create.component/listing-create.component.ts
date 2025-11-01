import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { ListingsApi } from '../api/listings-api';
import { CategoryItem, CreateListingRequest } from '../api/models';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-listing-create.component',
  imports: [CommonModule, FormsModule],
  standalone: true,
  templateUrl: './listing-create.component.html',
  styleUrls: ['./listing-create.component.scss'],
})
export class ListingCreateComponent {
  title = '';
  priceUsd: number | null = null;
  category: string = '';
  images: string[] = [];
  imagesText = '';
  sellerID = '11111111-1111-1111-1111-111111111111';
  loading = false;
  errorMsg: string | null = null;

  private router = inject(Router);
  private api = inject(ListingsApi);

  categoryPath = '';
  cats$!: Observable<CategoryItem[]>;

  // load categories on init
  ngOnInit(): void {
    this.loadCategories();
  }

  // handles the form submission to create a new listing
  onSubmit() {
    this.errorMsg = null;

    const title = (this.title || '').trim();
    if (!title) {
      this.errorMsg = 'title is required.';
      return;
    }

    const priceUsd = Number(this.priceUsd);
    if (!Number.isFinite(priceUsd) || priceUsd <= 0) {
      this.errorMsg = 'price is required.';
      return;
    }

    this.images = this.normalizeUrls(this.imagesText).slice(0, 6);

    const newListing: CreateListingRequest = {
      title,
      priceUsd: priceUsd,
      images: this.images.length ? this.images : null,
      sellerId: this.sellerID,
      categoryPath: this.categoryPath,
    };

    this.loading = true;
    this.api.createListing(newListing).subscribe({
      next: (response) => {
        this.loading = false;
        this.router.navigate(['/listings', response.id]);
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = err.message || 'Failed to create listing.';
      },
      complete: () => {
        this.loading = false;
      },
    });
  }

  //normalizes my urls, split them, trims whitespaces, filters if null etc, filters to allow only http starts
  normalizeUrls(input: string): string[] {
    return input
      .split(/[\n,]/g)
      .map((s) => s.trim())
      .filter(Boolean)
      .filter((u) => u.startsWith('http'));
  }

  // navigate back to the listings page
  back() {
    this.router.navigate(['/listings']);
  }

  // loads categories based on the current category path
  private loadCategories() {
    this.cats$ = this.api.getAllCategories(this.categoryPath);
  }

  // breadcrumbs methods to navigate categories
  crumbs(): string[] {
    return this.categoryPath?.split('/') ?? [];
  }

  // navigate to a specific category level
  goToLevel(index: number): void {
    if (index < 0) {
      this.categoryPath = '';
    } else {
      this.categoryPath = this.crumbs()
        .slice(0, index + 1)
        .join('/');
    }
    this.loadCategories();
  }

  // navigate into a child category
  goInto(child: CategoryItem): void {
    this.categoryPath = child.path;
    this.loadCategories();
  }

  // returns a pretty version of the segment for display
  pretty(seg: string): string {
    return seg
      .split('-')
      .filter(Boolean)
      .map((s) => s.charAt(0).toUpperCase() + s.slice(1))
      .join(' ');
  }
}
