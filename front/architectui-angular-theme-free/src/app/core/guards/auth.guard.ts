import { Injectable } from '@angular/core';
import {
  CanActivate,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  UrlTree,
  Router,
} from '@angular/router';
import { Observable } from 'rxjs';
import { TokenDecoder } from 'src/app/shared/utils/jwt.util';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {
  constructor(private router: Router, private tokenDecoder: TokenDecoder) {}

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    const token = localStorage.getItem('auth_token'); // Vérifiez si le token existe

    if (token) {

      return true; // Accès autorisé
    
    } else {
      // Redirigez vers la page de connexion si l'utilisateur n'est pas authentifié
      this.router.navigate(['/login']);
      return false;
    }
  }
}
