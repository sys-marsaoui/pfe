import { Component, OnInit } from '@angular/core';
import { RegisterBoxedServices } from './register-boxed.service';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-register-boxed',
  templateUrl: './register-boxed.component.html',
  styleUrls: ['register.component.scss']
})
export class RegisterBoxedComponent implements OnInit {
  userCredentials: any = {};
  post: string | null = null;
  loading = false;
  error: string | null = null;

  constructor(
    private registerBoxedServices: RegisterBoxedServices,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.post = params['post'] || null;
      if (!this.post) {
        this.error = 'Post ID is required to register.';
      }
    });
  }

  createUser() {
    if (!this.post) {
      this.error = 'Post ID is missing.';
      return;
    }
    if (this.userCredentials.password !== this.userCredentials.repeatpassword) {
      this.error = 'Passwords do not match.';
      return;
    }
    this.loading = true;
    this.error = null;

    this.registerBoxedServices.postData(this.userCredentials, this.post).subscribe({
      next: (response) => {
        console.log('Registration successful:', response);
        this.router.navigate(['/login']);
      },
      error: (err) => {
        console.error('Error posting data:', err);
        this.error = err.error?.msg || err.error?.message ||
          (err.status === 0
            ? 'Impossible de joindre le serveur. Vérifiez que le backend est démarré.'
            : 'Inscription impossible. Vérifiez que l’offre existe et que cet e-mail n’est pas déjà utilisé.');
        this.loading = false;
      },
      complete: () => {
        this.loading = false;
      }
    });
  }
}
