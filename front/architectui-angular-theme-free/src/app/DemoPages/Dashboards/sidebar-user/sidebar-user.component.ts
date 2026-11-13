// src/app/shared/sidebar/sidebar-user.component.ts
import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/core/services/auth-service';

@Component({
  selector: 'app-sidebar-user',
  templateUrl: './sidebar-user.component.html',
  styleUrls: ['./sidebar-user.component.scss']
})
export class SidebarUserComponent {
  @Input() collapsed: boolean = false;
  user: any = {};

  constructor(
    public router: Router,
    private authService: AuthService
  ) {
    this.user = this.authService.getUserDetails() || {};
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}