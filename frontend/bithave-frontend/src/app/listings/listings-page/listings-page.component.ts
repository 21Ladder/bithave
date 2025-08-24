import { Component } from '@angular/core';
import { inject } from '@angular/core';
import { ListingsApi } from '../../api/listings-api';
import { map, Observable } from 'rxjs';
import { ListingSummary, PageResponse } from '../../api/models';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-listings-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './listings-page.component.html',
  styleUrl: './listings-page.component.scss'
})
export class ListingsPageComponent {

  private readonly api = inject(ListingsApi);

  //holt mir den kompletten pageresponse, piped mit map sodass nur die items bleiben
  readonly items$: Observable<ListingSummary[]> = this.api.list().pipe(map(response => response.items));
}
