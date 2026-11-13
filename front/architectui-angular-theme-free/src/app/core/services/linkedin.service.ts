import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({ providedIn: 'root' })
export class LinkedInService {
  private readonly api = `${environment.apiUrl}/api/integrations/linkedin`;

  constructor(private http: HttpClient) {}

  getOfferAuthorizationUrl(postId: string): Observable<{ authorizationUrl: string }> {
    return this.http.post<{ authorizationUrl: string }>(`${this.api}/offers/${postId}/authorize`, {});
  }
}
