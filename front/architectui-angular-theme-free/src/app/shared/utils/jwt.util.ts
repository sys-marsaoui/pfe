import { Injectable } from '@angular/core';
import * as jwt_decode from "jwt-decode";

@Injectable({
  providedIn: 'root'
})
export class TokenDecoder {

 getDecodedAccessToken(token: string): any {
    try {
      return jwt_decode.jwtDecode(token);
    } catch(Error) {
      return null;
    }
  }

  getUser(): any{
    const token = localStorage.getItem('auth_token');
    return this.getDecodedAccessToken(token).user;
  }
}