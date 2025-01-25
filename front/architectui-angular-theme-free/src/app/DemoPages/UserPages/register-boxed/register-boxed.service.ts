import { Injectable, OnInit } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { ActivatedRoute } from '@angular/router';

// Accessing API URL
const apiUrl = environment.apiUrl;
@Injectable({
  providedIn: 'root'
})
export class RegisterBoxedServices implements OnInit {

  constructor(private http: HttpClient) { }
  ngOnInit(): void {
     
  }

  postData(data: any , postId: string): Observable<any> {
    const params = postId? new HttpParams().set('post', postId) : new HttpParams();
    return this.http.post<any>(apiUrl+'/add_user', data, {params});
  }
}
