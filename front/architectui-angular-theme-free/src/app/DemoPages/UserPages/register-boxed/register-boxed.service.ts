import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

// Accessing API URL
const apiUrl = environment.apiUrl;
@Injectable({
  providedIn: 'root'
})
export class RegisterBoxedServices {

  constructor(private http: HttpClient) { }

  postData(data: any): Observable<any> {
    return this.http.post<any>(apiUrl+'/addAccount', data);
  }
}
