import { Component, OnInit } from '@angular/core';
import { ListingDetail } from '../api/models';
import { Observable } from 'rxjs';
import { ListingsApi } from '../api/listings-api';
import { inject } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgOptimizedImage } from '@angular/common';

@Component({
  selector: 'app-listing-detail.component',
  standalone: true,
  imports: [CommonModule, FormsModule, NgOptimizedImage],
  templateUrl: './listing-detail.component.html',
  styleUrls: ['./listing-detail.component.scss']
})
export class ListingDetailComponent implements OnInit{

  listingDetail$!: Observable<ListingDetail>;
  private readonly api = inject(ListingsApi);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  id: string = '';

  isImageLoading = true;

  ngOnInit() {

    //sets the id to the actual id from the url path
    const id = this.route.snapshot.paramMap.get('id');
      if(!id){
        return;
      }
    this.listingDetail$ = this.api.get(id);
    console.log(this.listingDetail$)
  }

  goBack() {
    this.router.navigate(['/listings']);
  }

}
