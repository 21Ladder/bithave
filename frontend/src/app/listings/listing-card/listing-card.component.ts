import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { EurPipe } from '../../core/money.pipe';
import { ListingSummary } from '../api/models';

@Component({
  selector: 'app-listing-card',
  standalone: true,
  imports: [RouterLink, EurPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './listing-card.component.html',
  styleUrls: ['./listing-card.component.scss'],
})
export class ListingCardComponent {
  @Input({ required: true }) listing!: ListingSummary;

  // Local asset, the old placeholder service no longer exists
  readonly fallback = 'assets/no-image.svg';

  imageSrc(): string {
    return this.listing.thumbnailUrl || this.fallback;
  }

  onImageError(event: Event): void {
    (event.target as HTMLImageElement).src = this.fallback;
  }
}
