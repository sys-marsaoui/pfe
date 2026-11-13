import { Component } from '@angular/core';
import { ThemeOptions } from '../../../../../theme-options';
import { AuthService } from 'src/app/core/services/auth-service';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-user-box',
  templateUrl: './user-box.component.html',
  styleUrls: ['./user-box.component.scss']
})
export class UserBoxComponent {
  userAvatar = './assets/images/avatars/1.jpg'; // Change selon ton besoin

  constructor(
    public globals: ThemeOptions,
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService
  ) {}

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  get userName(): string {
    const user = this.authService.getUserDetails();
    return [user.firstName, user.lastName].filter(Boolean).join(' ') || 'Utilisateur';
  }

  logout(): void {
    this.authService.logout();
    this.toastr.success('Déconnexion réussie');
    this.router.navigate(['/login']);
  }
}
