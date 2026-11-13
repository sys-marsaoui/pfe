import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../services/auth-service';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService, private router: Router) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (req.url.includes('/authenticate') || req.url.includes('/refresh-token') ||
        /\/post\/[^/]+\/register(?:\?|$)/.test(req.url)) {
      return next.handle(req);
    }

    const token = this.authService.getToken();
    let authReq = req;

    if (token) {
      authReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token.trim()}`
        },
        withCredentials: true
      });
    }

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          this.authService.logout();
          this.router.navigate(['/login'], { queryParams: { error: 'Session expired' } });
        } else if (error.status === 403) {
          // Avoid logging out for /api/quizzes to debug the issue
          // Optionally show a user-friendly message
        }
        return throwError(() => error);
      })
    );
  }
}
