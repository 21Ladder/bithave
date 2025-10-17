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

  loading = true;
  errorMessage: string | null = null;
  listing: ListingDetail | null = null;
  id = '';
  btcPrice: number = 0;   // current USD per BTC coming from the Database ~ updated every 10 mins
  satsPrice: number = 0;  // calculated sats price

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get('id') || '';
    if (!this.id) {
      this.errorMessage = 'No ID.';
      this.loading = false;
      return;
    }

    // Load detail
    this.api.get(this.id).subscribe({
      next: data => {
        this.listing = data;
        this.loading = false;
        this.calculateSats(); 
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage =
          err.status === 404 ? 'Entry not found.'
        : err.status === 0   ? 'Network error – please check.'
        : 'Error loading.';
        this.loading = false;
      }
    });

    // Load BTC price (USD per BTC)
    this.api.getBtcPrice().subscribe({
      next: (res) => {
        console.log('response:', res);
        this.btcPrice = Number(res);
        console.log('BTC price loaded:', this.btcPrice);
      },
      error: () => {
        // show no btc price if error
      }
    });
  }

  private calculateSats() {
    if (this.listing && typeof this.listing.priceUsd === 'number' && this.btcPrice) {
      // Calculating the current price in Satoshis based on 1 BTC = 100,000,000 Satoshis
      this.satsPrice = Math.round(
        (this.listing.priceUsd / this.btcPrice) * 100_000_000
      );
    }
  }

  goBack() { this.router.navigate(['/listings']); }
  goToEdit() { this.router.navigate(['/listings', this.id, 'edit']); }
}
