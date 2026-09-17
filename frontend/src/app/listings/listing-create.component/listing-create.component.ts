import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiError } from '../../core/error.interceptor';
import { ListingsApi } from '../api/listings-api';
import { CategoryItem } from '../api/models';

@Component({
  selector: 'app-listing-create',
  standalone: true,
  imports: [ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './listing-create.component.html',
  styleUrls: ['../listing-form.scss'],
})
export class ListingCreateComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(ListingsApi);
  private readonly router = inject(Router);

  readonly categories = signal<CategoryItem[]>([]);
  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);

  // Price is entered in euros and sent as cents
  readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(120)]],
    priceEur: [0, [Validators.required, Validators.min(0.01)]],
    quantity: [1, [Validators.required, Validators.min(0)]],
    categoryPath: ['', [Validators.required]],
    imagesText: [''],
  });

  ngOnInit(): void {
    this.api.getAllCategories('').subscribe({
      next: (cats) => this.categories.set(cats),
      error: () => this.categories.set([]),
    });
  }

  // Drills into the tree so deeper paths can be picked
  onCategoryChange(path: string): void {
    this.form.controls.categoryPath.setValue(path);
    this.api.getAllCategories(path).subscribe({
      next: (cats) => {
        if (cats.length) this.categories.set(cats);
      },
      error: () => undefined,
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.error.set(null);
    const value = this.form.getRawValue();

    this.api
      .createListing({
        title: value.title.trim(),
        priceCents: Math.round(value.priceEur * 100),
        quantity: value.quantity,
        categoryPath: value.categoryPath,
        images: this.normalizeUrls(value.imagesText),
      })
      .subscribe({
        next: (listing) => this.router.navigate(['/listings', listing.id]),
        error: (err: ApiError) => {
          this.submitting.set(false);
          this.error.set(err.message);
        },
      });
  }

  // Accepts absolute URLs and the server side paths used by the seed data
  private normalizeUrls(raw: string): string[] {
    return raw
      .split(/[\n,]/)
      .map((s) => s.trim())
      .filter((s) => s.startsWith('http') || s.startsWith('/'))
      .slice(0, 6);
  }
}
