import { Routes } from '@angular/router';
import { ListingsPageComponent } from './listings/listings-page/listings-page.component';
import { ListingDetailComponent } from './listings/listing-detail/listing-detail.component';

export const routes: Routes = [
  { path: 'listings', component: ListingsPageComponent },
  { path: 'listings/:id', component: ListingDetailComponent },

  //leitet direkt auf listings weiter wenn nichts anderes angegeben ist
  { path: '', pathMatch: 'full', redirectTo: 'listings' }
];
