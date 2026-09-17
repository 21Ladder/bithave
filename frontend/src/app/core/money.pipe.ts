import { Pipe, PipeTransform } from '@angular/core';

// Prices travel as EUR cents, never as floats
@Pipe({ name: 'eur', standalone: true })
export class EurPipe implements PipeTransform {
  private readonly formatter = new Intl.NumberFormat('de-DE', {
    style: 'currency',
    currency: 'EUR',
  });

  transform(cents: number | null | undefined): string {
    if (cents === null || cents === undefined || !Number.isFinite(cents)) return '';
    return this.formatter.format(cents / 100);
  }
}

// Shared sats conversion, previously duplicated in two components
@Pipe({ name: 'sats', standalone: true })
export class SatsPipe implements PipeTransform {
  transform(cents: number | null | undefined, btcPriceEur: number | null | undefined): number | null {
    if (!cents || !btcPriceEur || btcPriceEur <= 0) return null;
    return Math.round((cents / 100 / btcPriceEur) * 100_000_000);
  }
}
