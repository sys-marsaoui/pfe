import { Injectable, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

const apiUrl = environment.apiUrl;

@Injectable({
  providedIn: 'root'
})
export class RegisterBoxedServices implements OnInit {
  constructor(private http: HttpClient) { }

  ngOnInit(): void {}

  postData(data: any, postId: string): Observable<any> {
    if (!postId) {
      throw new Error('postId is required');
    }
    const url = `${apiUrl}/post/${postId}/register`;
    return this.http.post<any>(url, data);
  }
}