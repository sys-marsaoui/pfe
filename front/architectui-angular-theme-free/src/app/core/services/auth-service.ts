import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { jwtDecode } from 'jwt-decode';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = environment.apiUrl;
  private readonly tokenKey = 'auth_token';
  private readonly refreshTokenKey = 'refresh_token';

  constructor(private http: HttpClient) {}

  Auth(data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/authenticate`, data).pipe(
      tap((response: any) => {
        if (response?.code === '200' && response.token && response.refreshToken) {
          this.saveTokens(response.token, response.refreshToken);
        }
      })
    );
  }

  saveTokens(accessToken: string, refreshToken: string): void {
    const isJwt = (token: string) => !!token && token.split('.').length === 3;
    if (!isJwt(accessToken)) {
      throw new Error('Invalid access token format');
    }
    if (!isJwt(refreshToken)) {
      throw new Error('Invalid refresh token format');
    }

    localStorage.setItem(this.tokenKey, accessToken);
    localStorage.setItem(this.refreshTokenKey, refreshToken);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.refreshTokenKey);
  }

  getUserDetails(): any {
    const token = this.getToken();
    if (token) {
      try {
        const decoded: any = jwtDecode(token);
        return decoded.user || {};
      } catch (error) {
        return {};
      }
    }
    return {};
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.refreshTokenKey);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/reset-password`, { token, newPassword });
  }
}
