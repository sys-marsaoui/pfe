import { Component, OnInit } from '@angular/core';
import { ThemeOptions } from '../../../../../theme-options';
import { AuthService } from 'src/app/core/services/auth-service';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-user-box',
  templateUrl: './user-box.component.html',
})
export class UserBoxComponent implements OnInit {

  constructor(
    public globals: ThemeOptions,
    private authService: AuthService, // Injection de AuthService
    private router: Router,           // Injection de Router
    private toastr: ToastrService     // Injection de ToastrService
  ) { }

  ngOnInit() { }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }
  
  logout() {
    // Appel de la fonction logout dans AuthService
    this.authService.logout();
    this.toastr.success('Logged out successfully'); // Notification de déconnexion
    this.router.navigate(['/login']); // Redirection vers la page de login
  }
}
