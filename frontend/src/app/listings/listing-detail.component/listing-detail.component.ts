import { Component, inject, OnInit } from '@angular/core';
import { ListingsApi } from '../api/listings-api';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ListingDetail } from '../api/models';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

type Vm =
  | { kind: 'loading' }
  | { kind: 'loaded'; data: ListingDetail }
  | { kind: 'error'; message: string };

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

  vm: Vm = { kind: 'loading' };

  ngOnInit() {

    // Get the ID of the listing via route parameters
    const id = this.route.snapshot.paramMap.get('id');
    //if no ID is present show error
    if (!id) { this.vm = { kind: 'error', message: 'No ID.' }; return; }

    // Fetch the listing details, next means the successful response, error if 404 or others
    this.api.get(id).subscribe({
      next: data => this.vm = { kind: 'loaded', data },
      error: (err: HttpErrorResponse) => {
        const msg = err.status === 404 ? 'Entry not found.'
                  : err.status === 0   ? 'Network error – please check.'
                                       : 'Error loading.';
        this.vm = { kind: 'error', message: msg };
      }
    });
  }

  goBack(){ this.router.navigate(['/listings']); }
}
