import { Routes } from '@angular/router';
import { ListingsPageComponent } from './listings/listings-page.component/listings-page.component';
import { ListingDetailComponent } from './listings/listing-detail.component/listing-detail.component';
import { ListingCreateComponent } from './listings/listing-create.component/listing-create.component';
import { ListingEditComponent } from './listings/listing-edit.component/listing-edit.component'; 

export const routes: Routes = [
  { path: 'listings', component: ListingsPageComponent },
  { path: 'listings/new', component: ListingCreateComponent },
  { path: 'listings/:id/edit', component: ListingEditComponent },
  { path: 'listings/:id', component: ListingDetailComponent },

  //leitet direkt auf listings weiter wenn nichts anderes angegeben ist
  { path: '', pathMatch: 'full', redirectTo: 'listings' }
];
