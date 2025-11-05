import { Component, inject, OnInit } from '@angular/core';
import { ListingsApi } from '../api/listings-api';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ListingDetail } from '../api/models';
import { CartService } from '../../cart/cart.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-listing-detail',
  imports: [CommonModule, FormsModule],
  standalone: true,
  templateUrl: './listing-detail.component.html',
  styleUrl: './listing-detail.component.scss',
})
export class ListingDetailComponent implements OnInit {
  private api = inject(ListingsApi);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private cartService = inject(CartService);

  loading = true;
  errorMessage: string | null = null;
  listing: ListingDetail | null = null;
  id = '';
  btcPrice: number = 0; // current USD per BTC coming from the Database ~ updated every 10 mins
  satsPrice: number = 0; // calculated sats price

  cartStatus: string | null = null;
  cartError: string | null = null;
  cartLoading = false;

  // will load listing detail and btc price on init
  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get('id') || '';
    if (!this.id) {
      this.errorMessage = 'No ID.';
      this.loading = false;
      return;
    }

    // Load detail
    this.api.getDetailListing(this.id).subscribe({
      next: (data: ListingDetail) => {
        this.listing = data;
        this.loading = false;
        this.calculateSats();
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

    // Load BTC price (USD per BTC)
    this.api.getBtcPrice().subscribe({
      next: (res: number) => {
        this.btcPrice = res;
        console.log('BTC price loaded:', this.btcPrice);
        this.calculateSats();
      },
      error: () => {
        // show no btc price if error
      },
    });
  }

  private calculateSats() {
    if (this.listing && typeof this.listing.priceUsd === 'number' && this.btcPrice) {
      // Calculating the current price in Satoshis based on 1 BTC = 100,000,000 Satoshis
      this.satsPrice = Math.round((this.listing.priceUsd / this.btcPrice) * 100_000_000);
    }
  }

  goBack() {
    this.router.navigate(['/listings']);
  }
  goToEdit() {
    this.router.navigate(['/listings', this.id, 'edit']);
  }

  addToCart(): void {
    if (!this.listing) {
      return;
    }

    if (this.listing.availableQuantity < 1) {
      this.cartStatus = null;
      this.cartError = 'Artikel ist derzeit nicht verfügbar.';
      return;
    }

    this.cartLoading = true;
    this.cartStatus = null;
    this.cartError = null;

    this.cartService.addItem(this.listing.id).subscribe({
      next: () => {
        this.cartLoading = false;
        this.cartStatus = 'Artikel wurde dem Warenkorb hinzugefügt.';
        if (this.listing) {
          this.listing = {
            ...this.listing,
            reservedQuantity: this.listing.reservedQuantity + 1,
            availableQuantity: Math.max(0, this.listing.availableQuantity - 1),
          };
        }
      },
      error: (err: HttpErrorResponse) => {
        this.cartLoading = false;
        this.cartError =
          err.status === 409
            ? 'Nicht genügend Bestand verfügbar.'
            : 'Artikel konnte nicht in den Warenkorb gelegt werden.';
      },
    });
  }
}
