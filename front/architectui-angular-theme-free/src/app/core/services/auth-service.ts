import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';


@Injectable({
  providedIn: 'root'
})
export class AuthService {
    private apiUrl = 'http://localhost:8080';
    private readonly tokenKey = 'auth_token';

    constructor(private http:HttpClient){}

    Auth(data:any):Observable<any>{
        return this.http.post(`${this.apiUrl}/authenticate`,data)
      }
      saveToken(token: string): void {
        localStorage.setItem(this.tokenKey, token);
      }

      getToken(): string | null {
        return localStorage.getItem(this.tokenKey);
      }
      logout() {
        localStorage.removeItem('auth_token'); // Suppression du token d'authentification
        console.log('User logged out');
      }

        // Vérifier si l'utilisateur est connecté en vérifiant le token
  isLoggedIn(): boolean {
    return !!localStorage.getItem('auth_token'); // Remplace par ta méthode de stockage du token
  }

  registerCandidateForPost(postId: string, candidate: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/post/${postId}/register`, candidate);
  }
}