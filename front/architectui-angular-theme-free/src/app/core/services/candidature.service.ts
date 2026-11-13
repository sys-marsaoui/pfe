import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { Candidature } from '../models/candidature.model';

@Injectable({ providedIn: 'root' })
export class CandidatureService {
  private readonly api = `${environment.apiUrl}/api/admin/candidatures`;

  constructor(private http: HttpClient) {}

  applyWithCv(postId: string): Observable<Candidature> {
    return this.http.post<Candidature>(`${this.api}/apply/${postId}`, {});
  }

  /** GET /post/{postId} */
  getByPost(postId: string): Observable<Candidature[]> {
    return this.http
      .get<Candidature[]>(`${this.api}/post/${postId}`)
      .pipe(catchError(this.handleError));
  }

  /** PATCH /{id}/accept  →  { interviewDateTime: ISO-string } */
  accept(id: string, interviewDateTime: string): Observable<Candidature> {
    return this.http
      .patch<Candidature>(`${this.api}/${id}/accept`, { interviewDateTime })
      .pipe(catchError(this.handleError));
  }

  /** PATCH /{id}/reject */
  reject(id: string): Observable<Candidature> {
    return this.http
      .patch<Candidature>(`${this.api}/${id}/reject`, {})
      .pipe(catchError(this.handleError));
  }

  private handleError(err: HttpErrorResponse) {
    const msg = err.error?.message || err.message || 'Unknown error';
    return throwError(() => new Error(msg));
  }
  getAllByPosts(): Observable<{ [postId: string]: Candidature[] }> {
  return this.http
    .get<{ [postId: string]: Candidature[] }>(`${this.api}/all-posts`)
    .pipe(catchError(this.handleError));
}
}
