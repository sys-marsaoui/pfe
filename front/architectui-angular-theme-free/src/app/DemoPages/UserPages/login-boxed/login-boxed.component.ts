import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from 'src/app/core/services/auth-service';

interface Credentials {
  email: string;
  password: string;
}

@Component({
  selector: 'app-login-boxed',
  templateUrl: './login-boxed.component.html',
  styleUrls: ['./login-boxed.component.scss']
})
export class LoginBoxedComponent {
  credentials: Credentials = { email: '', password: '' };
  focused = { email: false, password: false };
  showPassword = false;
  rememberMe = false;
  isLoading = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService
  ) {}

  onFocus(field: 'email' | 'password'): void {
    this.focused[field] = true;
  }

  onBlur(field: 'email' | 'password'): void {
    this.focused[field] = false;
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  connect(): void {
    if (!this.credentials.email || !this.credentials.password) {
      this.toastr.error('Veuillez remplir tous les champs');
      return;
    }

    this.isLoading = true;

    this.authService.Auth(this.credentials).subscribe({
      next: (response) => {
        this.isLoading = false;
        if (response?.code === '200' && response.token && response.refreshToken) {
          const user = this.authService.getUserDetails();
          const role = user?.role;

          this.toastr.success('Connexion réussie !');

          if (role === 'ADMIN') {
            this.router.navigate(['/postes']);
          } else if (role === 'CANDIDATE') {
            this.router.navigate(['/profile']);
          } else {
            this.toastr.warning('Rôle inconnu, redirection par défaut');
            this.router.navigate(['/']);
          }
        } else {
          this.toastr.error(response?.code === '400'
            ? 'Identifiants incorrects'
            : 'Le serveur a renvoyé une réponse de connexion invalide.');
        }
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Login error:', err);
        this.toastr.error(err.status === 0
          ? 'Impossible de joindre le serveur. Vérifiez que le backend est démarré.'
          : 'Erreur du serveur pendant la connexion.');
      }
    });
  }
}
