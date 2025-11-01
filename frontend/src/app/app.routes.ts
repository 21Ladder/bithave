import { Routes } from '@angular/router';
import { ListingsPageComponent } from './listings/listings-page.component/listings-page.component';
import { ListingDetailComponent } from './listings/listing-detail.component/listing-detail.component';
import { ListingCreateComponent } from './listings/listing-create.component/listing-create.component';
import { ListingEditComponent } from './listings/listing-edit.component/listing-edit.component';

// here are all the routes for the listings module
export const routes: Routes = [
  { path: 'listings', component: ListingsPageComponent },
  { path: 'listings/new', component: ListingCreateComponent },
  { path: 'listings/:id/edit', component: ListingEditComponent },
  { path: 'listings/:id', component: ListingDetailComponent },

  // default route
  { path: '', pathMatch: 'full', redirectTo: 'listings' },
];
