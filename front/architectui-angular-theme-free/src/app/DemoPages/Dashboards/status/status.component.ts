import { Component, OnInit } from '@angular/core';
import { AuthService } from 'src/app/core/services/auth-service';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-status',
  templateUrl: './status.component.html',
  styleUrls: ['./status.component.scss']
})
export class StatusComponent implements OnInit {
  progress: any = { percentage: 0, stage: 'REGISTERED' };
  loading = true;
  error: string | null = null;
  isSidebarCollapsed = false;
  currentUser: any = {};
  history: any[] = [];

  private progressMap: { [key: string]: number } = {
    'REGISTERED': 20,
    'CV_UPLOADED': 40,
    'CV_VERIFIED': 60,
    'QUIZ_ACCEPTED': 70,
    'QUIZ_PASSED': 85,
    'ENTRETIEN': 100
  };

  constructor(private authService: AuthService, private http: HttpClient) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.error = 'Please log in to view your progress';
      this.loading = false;
      return;
    }
    this.currentUser = this.authService.getUserDetails() || {};
    this.fetchProgress();
    this.http.get<any[]>(`${environment.apiUrl}/api/progress/history`).subscribe({
      next: (events) => this.history = events || [],
      error: () => this.history = []
    });
  }

  fetchProgress(): void {
    const token = this.authService.getToken();
    if (!token) {
      this.error = 'No authentication token found';
      this.loading = false;
      return;
    }

    this.http.get<any>(`${environment.apiUrl}/api/progress/user`, {
      headers: new HttpHeaders({ 'Authorization': `Bearer ${token.trim()}` })
    }).subscribe({
      next: (data) => {
        const stage = data.stage || 'REGISTERED';
        this.progress = {
          ...data,
          percentage: data.percentage ?? this.progressMap[stage] ?? 0,
          stage
        };
        this.loading = false;
      },
      error: (error) => {
        this.error = error.error?.error || 'Failed to fetch progress';
        this.loading = false;
      }
    });
  }

  toggleSidebar(): void {
    this.isSidebarCollapsed = !this.isSidebarCollapsed;
  }
}
