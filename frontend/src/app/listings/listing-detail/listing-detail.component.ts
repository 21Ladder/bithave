import { Component, OnInit } from '@angular/core';
import { ListingDetail } from '../api/models';
import { catchError, filter, map, Observable, of, shareReplay, startWith, switchMap } from 'rxjs';
import { ListingsApi } from '../api/listings-api';
import { inject } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgOptimizedImage } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';

// type for the viewmodel which will be initialized later
type ListingDetailVm =
  | { kind: 'loading' }
  | { kind: 'loaded'; data: ListingDetail }
  | { kind: 'error'; code?: number; message: string };

@Component({
  selector: 'app-listing-detail.component',
  standalone: true,
  imports: [CommonModule, FormsModule, NgOptimizedImage],
  templateUrl: './listing-detail.component.html',
  styleUrls: ['./listing-detail.component.scss']
})
export class ListingDetailComponent implements OnInit{

  // dependencies injected via Angular's dependency injection
  private readonly api = inject(ListingsApi);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  vm$!: Observable<ListingDetailVm>;


  id$!: Observable<string>;

  isImageLoading = true;

  ngOnInit() {

    var route = this.route; 
    var api = this.api;

    this.id$ = this.route.paramMap.pipe(
      map(param => param.get('id')),
      filter((id): id is string => !!id)   //only real IDs are allowed
    );

    this.vm$ = this.id$.pipe(
      // depending on what is received, the view model will be updated (data, error), later I can ask for it in the html via *ngIf="vm.kind === 'xxxxxx'"
      switchMap(id =>
        this.api.get(id).pipe(
          map(data => ({ kind: 'loaded', data } as const)),
          catchError((err: HttpErrorResponse) => {
            const msg =
              err.status === 404 ? 'Eintrag nicht gefunden.' :
              err.status === 0   ? 'Netzwerkfehler – bitte prüfen.' :
                                  'Fehler beim Laden.';
            return of({ kind: 'error', code: err.status, message: msg } as const);
          }),
          startWith({ kind: 'loading' } as const)
        )
      ),
      shareReplay(1) //caches the last value, so if I use async in the html template, there will be only one http request (reduces loading times and server load)
    );
  }

  goBack() {
    this.router.navigate(['/listings']);
  }
}
