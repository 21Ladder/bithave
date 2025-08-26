import { Routes } from '@angular/router';
import { ListingsPageComponent } from './listings/listings-page.component';

export const routes: Routes = [
  { path: 'listings', component: ListingsPageComponent },

  //leitet direkt auf listings weiter wenn nichts anderes angegeben ist
  { path: '', pathMatch: 'full', redirectTo: 'listings' }
];
