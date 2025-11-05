import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { EditListingRequest, ListingDetail } from '../api/models';
import { ListingsApi } from '../api/listings-api';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-listing-edit',
  imports: [FormsModule, CommonModule],
  standalone: true,
  templateUrl: './listing-edit.component.html',
  styleUrls: ['./listing-edit.component.scss'],
})
export class ListingEditComponent {
  id = '';
  title = '';
  priceUsd: number = 0;
  quantity: number = 0;
  status = '';
  imagesText = '';
  images!: string[];
  loading = false;
  saving = false;
  complete = false;
  errorMessage: string | null = null;

  private api = inject(ListingsApi);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  listingToBeEdited!: ListingDetail;
  editedListing!: EditListingRequest;

  // loads listing detail on init for editing
  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get('id') ?? '';
    if (!this.id) {
      this.errorMessage = 'No ID.';
      this.loading = false;
      return;
    }

    if (this.imagesText != null) {
      this.images = this.normalizeUrls(this.imagesText);
    }

    // Load detail for editing via API
    this.api.getDetailListing(this.id).subscribe({
      next: (data: ListingDetail) => {
        this.listingToBeEdited = data;
        this.title = data.title;
        this.priceUsd = data.priceUsd;
        this.quantity = data.quantity;
        this.status = data.status;

        if (data.images != null) {
          this.imagesText = data.images.join('\n');
        }

        this.loading = false;
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage =
          err.status === 404
            ? 'Entry not found.'
            : err.status === 0
            ? 'Network error – please check.'
            : 'Error loading.';
        this.loading = false;
      },
    });
  }

  // handles the form submission to save edited listing
  onSave(f: NgForm) {
    this.saving = true;
    this.images = this.normalizeUrls(this.imagesText);

    this.editedListing = {
      title: this.title,
      priceUsd: this.priceUsd,
      quantity: this.quantity,
      status: this.status,
      images: this.images,
    };

    this.api.editListing(this.id, this.editedListing).subscribe({
      next: (response: ListingDetail) => {
        this.loading = false;
      },
      error: (err: HttpErrorResponse) => {
        this.loading = false;
        this.errorMessage = err.message || 'Failed to edit listing.';
      },
      complete: () => {
        this.loading = false;
        this.complete = true;
        this.saving = false;
      },
    });
  }

  // normalizes image URLs from textarea input, later on will be uploaded images to server, not just URLs!
  normalizeUrls(input: string): string[] {
    return input
      .split(/[\n,]/g)
      .map((s) => s.trim())
      .filter(Boolean)
      .filter((u) => u.startsWith('http'))
      .slice(0, 6);
  }

  // navigates back to the listing details page
  backToDetails() {
    this.router.navigate(['/listings', this.listingToBeEdited.id]);
  }
}
