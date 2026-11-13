// src/app/guards/auth.guard.ts
import { Injectable } from '@angular/core';
import {
  CanActivate,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  UrlTree,
  Router,
} from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { TokenDecoder } from 'src/app/shared/utils/jwt.util';
import { environment } from 'src/environments/environment';
import { AuthService } from '../services/auth-service';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {

  constructor(
    private router: Router,
    private tokenDecoder: TokenDecoder,
    private http: HttpClient,
    private authService: AuthService
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    const token = localStorage.getItem('auth_token');
    if (!token) {
      this.router.navigate(['/login']);
      return false;
    }

    // Allow non-quiz routes
    if (!state.url.startsWith('/quiz/')) {
      return true;
    }

    // Get postId from query params
    const postId = route.queryParamMap.get('postId');
    if (!postId) {
      this.router.navigate(['/profile'], { queryParams: { error: 'missing-post' } });
      return false;
    }

    // ONE API CALL: Check acceptance
    return this.http.get<boolean>(
      `${environment.apiUrl}/api/quizzes/acceptance?postId=${postId}`,
      {
        headers: new HttpHeaders({ 'Authorization': `Bearer ${token}` }),
        withCredentials: true
      }
    ).pipe(
      map(isAccepted => {
        if (isAccepted) {
          return true;
        } else {
          this.router.navigate(['/profile'], { queryParams: { error: 'unauthorized' } });
          return false;
        }
      }),
      catchError(err => {
        console.error('Quiz acceptance check failed:', err);
        this.router.navigate(['/profile'], { queryParams: { error: 'unauthorized' } });
        return of(false);
      })
    );
  }
}