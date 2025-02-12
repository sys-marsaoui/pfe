import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root',
})
export class TechnologyService {
    private baseUrl = environment.apiUrl + '/api/technologies'; // Backend API URL

    constructor(private http: HttpClient) {}
  
    
    // Create a new technology
    create(technology: any): Observable<any> {
      return this.http.post(this.baseUrl, technology);
    }
   

  // Get all
  getAll(): Observable<any[]> {
    return this.http.get<any[]>(this.baseUrl);
  }
}