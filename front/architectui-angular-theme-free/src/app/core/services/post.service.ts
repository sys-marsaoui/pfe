import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root',
})
export class PostService {
  private baseUrl = environment.apiUrl + '/api/posts';
  private atsUrl = environment.apiUrl + '/api/ats';

  constructor(private http: HttpClient) {}

  getStat(postId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/${postId}/stat`);
  }

  getStatByUser(postId: string, userId: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${postId}/stat/${userId}`);
  }

  getAllPosts(): Observable<any[]> {
    return this.http.get<any[]>(this.baseUrl);
  }

  getPostById(postId: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${postId}`);
  }
  
  createPost(post: any): Observable<any> {
    return this.http.post(this.baseUrl, post);
  }

  updatePost(id: string, post: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}`, post);
  }

  deletePost(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  compareCvsToPostByPostId(postId: string): Observable<AtsMatchResult[]> {
    const headers = new HttpHeaders({
      'Authorization': 'Bearer ' + localStorage.getItem('token') // Adjust based on your auth mechanism
    });
    return this.http.get<AtsMatchResult[]>(`${this.atsUrl}/compare/post/${postId}`, { headers });
  }
}

export interface AtsMatchResult {
  userId: number;
  candidateName: string;
  matchScore: number;
  matchDetails: { [key: string]: any };
}