import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ListingSummary } from '../api/models';

@Component({
  selector: 'app-listing-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './listing-card.component.html',
  styleUrls: ['./listing-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ListingCardComponent {
  @Input({ required: true }) listing!: ListingSummary;
  @Output() selected = new EventEmitter<string>();

  readonly fallback = 'https://via.placeholder.com/160x120?text=No+Image';

  onClick(): void {
    this.selected.emit(this.listing.id);
  }

  imageSrc(): string {
    return this.listing.thumbnailUrl && this.listing.thumbnailUrl.trim().length > 0
      ? this.listing.thumbnailUrl
      : this.fallback;
  }
}
