import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { PostService } from 'src/app/core/services/post.service';
import { AuthService } from 'src/app/core/services/auth-service';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
  selector: 'app-user-details',
  templateUrl: './user-details.component.html',
  styleUrls: ['./user-details.component.scss']
})
export class UserDetailsComponent implements OnInit {
  user: any = null;
  cvData: any = null;
  postName: string = 'N/A';
  postId: string | null = null;          
  progress: any = { stage: 'N/A', percentage: 0 };
  loading = false;
  error: string | null = null;
  isDownloading = false;
  isViewing = false;
  acceptingQuiz = false;
  history: any[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private postService: PostService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    const userId = this.route.snapshot.paramMap.get('id');
    if (userId) {
      this.loadUserDetails(userId);
    } else {
      this.error = 'User ID not found.';
      this.loading = false;
    }
  }

  private loadUserDetails(userId: string): void {
    this.loading = true;
    this.error = null;

    const token = this.authService.getToken();
    if (!token) {
      this.error = 'Session expired. Please log in again.';
      this.loading = false;
      this.router.navigate(['/login']);
      return;
    }

    forkJoin([
      this.http.get<any>(`${environment.apiUrl}/users/${userId}`, {
        headers: new HttpHeaders({ 'Authorization': `Bearer ${token}` })
      }).pipe(
        catchError(err => {
          console.error('Error loading user:', err);
          if (err.status === 403) {
            this.error = 'Access denied to user details.';
          } else {
            this.error = 'Failed to load user details.';
          }
          return of(null);
        })
      ),
      this.http.get<any>(`${environment.apiUrl}/api/profile/cv-data/${userId}`, {
        headers: new HttpHeaders({ 'Authorization': `Bearer ${token}` })
      }).pipe(
        catchError(err => {
          console.error('Error loading CV data:', err);
          if (err.status === 403) {
            this.error = 'Access denied to CV data.';
          } else if (err.status === 404) {
            this.cvData = null;
          }
          return of(null);
        })
      )
    ]).subscribe({
      next: ([user, cvData]) => {
        if (user) {
          this.user = user;

          this.postId = user.postId || null;

          console.log('Loaded postId:', this.postId);

          this.loadPostName();
          this.loadProgress();
        }
        if (cvData) {
          this.cvData = {
            ...cvData,
            experiences: this.parseArray(cvData.experiences),
            projects: this.parseArray(cvData.projects),
            skills: this.parseArray(cvData.skills),
            education: this.parseArray(cvData.education)
          };
        }
        this.loadHistory(userId);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  private parseArray(value: any): any[] {
    if (!value) return [];
    if (Array.isArray(value)) return value;
    try {
      const parsed = JSON.parse(value);
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  }

  private loadHistory(userId: string): void {
    this.http.get<any[]>(`${environment.apiUrl}/api/progress/history/${userId}`).subscribe({
      next: (events) => this.history = events || [],
      error: () => this.history = []
    });
  }

  private loadPostName(): void {
    if (!this.postId) {
      this.postName = 'N/A';
      return;
    }

    this.postService.getPostById(this.postId).subscribe({
      next: (post: any) => {
        this.postName = post.name || post.title || 'Unnamed Post';
      },
      error: (err) => {
        console.error('Error loading post:', err);
        this.postName = 'N/A';
      }
    });
  }

  private loadProgress(): void {
    const token = this.authService.getToken();
    if (!token) {
      this.error = 'Session expired. Please log in again.';
      this.router.navigate(['/login']);
      return;
    }

    this.http.get(`${environment.apiUrl}/api/progress/user/${this.user.id}`, {
      headers: new HttpHeaders({ 'Authorization': `Bearer ${token}` })
    }).pipe(
      catchError(err => {
        console.error('Error loading progress:', err);
        if (err.status === 403) {
          this.progress = { stage: 'Access Denied', percentage: 0 };
        }
        return of({ stage: 'N/A', percentage: 0 });
      })
    ).subscribe({
      next: (progress: any) => {
        this.progress = progress;
      }
    });
  }

  acceptForQuiz(): void {
    if (!this.postId) {
      this.error = 'Post ID is missing.';
      return;
    }

    this.acceptingQuiz = true;
    this.error = null;

    const token = this.authService.getToken();
    if (!token) {
      this.error = 'Session expired. Please log in again.';
      this.acceptingQuiz = false;
      this.router.navigate(['/login']);
      return;
    }

    const url = `${environment.apiUrl}/api/progress/accept-quiz/${this.user.id}/${this.postId}`;

    this.http.post<any>(url, null, {
      headers: new HttpHeaders({ Authorization: `Bearer ${token}` })
    }).subscribe({
      next: (response) => {
        this.progress = response;         
        this.acceptingQuiz = false;
      },
      error: (err) => {
        console.error('Accept quiz error:', err);
        this.error = err.error?.error ?? 'Failed to accept user for quiz.';
        this.acceptingQuiz = false;
      }
    });
  }

  downloadCv(): void {
    if (!this.cvData?.cvPath) {
      this.error = 'No CV available for download.';
      return;
    }

    this.isDownloading = true;
    const token = this.authService.getToken();
    if (!token) {
      this.error = 'Session expired. Please log in again.';
      this.isDownloading = false;
      this.router.navigate(['/login']);
      return;
    }

    this.http.get(`${environment.apiUrl}/api/profile/download-cv/${this.user.id}`, {
      headers: new HttpHeaders({ 'Authorization': `Bearer ${token}` }),
      responseType: 'blob'
    }).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `cv_user_${this.user.id}.${this.cvData.cvPath.split('.').pop()}`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        this.isDownloading = false;
      },
      error: (err) => {
        console.error('Error downloading CV:', err);
        this.error = err.status === 403 ? 'Access denied to download CV.' : 'Failed to download CV.';
        this.isDownloading = false;
      }
    });
  }

  viewCv(): void {
    if (!this.cvData?.cvPath) {
      this.error = 'No CV available to view.';
      return;
    }

    this.isViewing = true;
    const token = this.authService.getToken();
    if (!token) {
      this.error = 'Session expired. Please log in again.';
      this.isViewing = false;
      this.router.navigate(['/login']);
      return;
    }

    this.http.get(`${environment.apiUrl}/api/profile/download-cv/${this.user.id}`, {
      headers: new HttpHeaders({ 'Authorization': `Bearer ${token}` }),
      responseType: 'blob'
    }).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        window.open(url, '_blank');
        window.URL.revokeObjectURL(url);
        this.isViewing = false;
      },
      error: (err) => {
        console.error('Error viewing CV:', err);
        this.error = err.status === 403 ? 'Access denied to view CV.' : 'Failed to view CV.';
        this.isViewing = false;
      }
    });
  }

  backToList(): void {
    this.router.navigate(['/users']);
  }

  editUser(): void {
    this.router.navigate([`/users/edit/${this.user.id}`]);
  }
}
