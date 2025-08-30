import { Component, inject, OnInit } from '@angular/core';
import { ListingsApi } from '../api/listings-api';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ListingDetail } from '../api/models';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-listing-detail',
  imports: [CommonModule, FormsModule],
  standalone: true,
  templateUrl: './listing-detail.component.html',
})
export class ListingDetailComponent implements OnInit {
  private api = inject(ListingsApi);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  // simple state flags/values
  loading = true;
  errorMessage: string | null = null;
  listing: ListingDetail | null = null;
  id: string = '';

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get('id') || '';
    if (!this.id) {
      this.errorMessage = 'No ID.';
      this.loading = false;
      return;
    }

    this.api.get(this.id).subscribe({
      next: data => {
        this.listing = data;
        this.loading = false;
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage =
          err.status === 404 ? 'Entry not found.'
          : err.status === 0   ? 'Network error – please check.'
                               : 'Error loading.';
        this.loading = false;
      }
    });
  }

  goBack() { this.router.navigate(['/listings']); }
  goToEdit() { this.router.navigate(['/listings', this.id, 'edit']); }
}