import { ChangeDetectionStrategy, Component, Input, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiError } from '../../core/error.interceptor';
import { ListingsApi } from '../api/listings-api';

@Component({
  selector: 'app-listing-edit',
  standalone: true,
  imports: [ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './listing-edit.component.html',
  styleUrls: ['../listing-form.scss'],
})
export class ListingEditComponent implements OnInit {
  // Bound from the route via withComponentInputBinding
  @Input() id = '';

  private readonly fb = inject(FormBuilder);
  private readonly api = inject(ListingsApi);
  private readonly router = inject(Router);

  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(120)]],
    priceEur: [0, [Validators.required, Validators.min(0.01)]],
    quantity: [0, [Validators.required, Validators.min(0)]],
    status: ['ACTIVE', [Validators.required]],
    imagesText: [''],
  });

  ngOnInit(): void {
    this.api.getDetailListing(this.id).subscribe({
      next: (listing) => {
        this.form.patchValue({
          title: listing.title,
          priceEur: listing.priceCents / 100,
          quantity: listing.quantity,
          status: listing.status,
          imagesText: (listing.images ?? []).join('\n'),
        });
        this.loading.set(false);
      },
      error: (err: ApiError) => {
        this.loading.set(false);
        this.error.set(err.message);
      },
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.error.set(null);
    const value = this.form.getRawValue();

    this.api
      .editListing(this.id, {
        title: value.title.trim(),
        priceCents: Math.round(value.priceEur * 100),
        quantity: value.quantity,
        status: value.status,
        images: this.normalizeUrls(value.imagesText),
      })
      .subscribe({
        next: () => this.router.navigate(['/listings', this.id]),
        error: (err: ApiError) => {
          // Reset saving here, the old version left the form locked forever on error
          this.saving.set(false);
          this.error.set(err.message);
        },
      });
  }

  private normalizeUrls(raw: string): string[] {
    return raw
      .split(/[\n,]/)
      .map((s) => s.trim())
      .filter((s) => s.startsWith('http') || s.startsWith('/'))
      .slice(0, 6);
  }
}
