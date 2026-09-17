import { ChangeDetectionStrategy, Component, Input, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { CartService } from '../../cart/cart.service';
import { ApiError } from '../../core/error.interceptor';
import { EurPipe, SatsPipe } from '../../core/money.pipe';
import { ListingsApi } from '../api/listings-api';
import { ListingDetail } from '../api/models';

@Component({
  selector: 'app-listing-detail',
  standalone: true,
  imports: [FormsModule, RouterLink, EurPipe, SatsPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './listing-detail.component.html',
  styleUrls: ['./listing-detail.component.scss'],
})
export class ListingDetailComponent implements OnInit {
  // Bound from the route via withComponentInputBinding
  @Input() id = '';

  private readonly api = inject(ListingsApi);
  private readonly cartService = inject(CartService);
  readonly auth = inject(AuthService);

  readonly listing = signal<ListingDetail | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly status = signal<string | null>(null);
  readonly adding = signal(false);
  readonly btcPrice = signal<number | null>(null);
  readonly activeImage = signal(0);

  quantity = 1;
  readonly fallback = 'assets/no-image.svg';

  ngOnInit(): void {
    this.api.getDetailListing(this.id).subscribe({
      next: (listing) => {
        this.listing.set(listing);
        this.loading.set(false);
      },
      error: (err: ApiError) => {
        this.loading.set(false);
        this.error.set(err.message);
      },
    });

    this.api.getBtcPrice().subscribe({
      next: (price) => this.btcPrice.set(price.priceEur),
      error: () => this.btcPrice.set(null),
    });
  }

  images(): string[] {
    const images = this.listing()?.images ?? [];
    return images.length ? images : [this.fallback];
  }

  selectImage(index: number): void {
    this.activeImage.set(index);
  }

  onImageError(event: Event): void {
    (event.target as HTMLImageElement).src = this.fallback;
  }

  addToCart(): void {
    const listing = this.listing();
    if (!listing) return;

    this.adding.set(true);
    this.status.set(null);
    this.error.set(null);

    this.cartService.addItem(listing.id, this.quantity).subscribe({
      next: () => {
        this.adding.set(false);
        this.status.set('Added to your cart.');
        // Re-read instead of guessing, the server owns the reservation
        this.api.getDetailListing(this.id).subscribe({
          next: (fresh) => this.listing.set(fresh),
          error: () => undefined,
        });
      },
      error: (err: ApiError) => {
        this.adding.set(false);
        this.error.set(err.message);
      },
    });
  }
}
