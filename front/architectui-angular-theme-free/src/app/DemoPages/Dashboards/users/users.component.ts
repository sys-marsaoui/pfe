import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { PostService } from 'src/app/core/services/post.service';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/core/services/auth-service';

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss']
})
export class UsersComponent implements OnInit {
  users: any[] = [];
  loading = false;
  error: string | null = null;
  searchTerm: string = '';
  postMap: Map<string, string> = new Map();
  sortConfig: { active: string; direction: string } = { active: '', direction: '' };
  selectedUser: any | null = null;
  atsMatches: any[] = [];
  atsLoading = false;
  atsError: string | null = null;

  constructor(
    private http: HttpClient,
    private postService: PostService,
    private router: Router,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.loadUsers();
  }

  viewUser(userId: string): void {
    this.router.navigate(['/users', userId]);
  }

  showAtsMatches(user: any): void {
    this.selectedUser = user;
    this.atsMatches = [];
    this.atsLoading = true;
    this.atsError = null;

    const token = this.authService.getToken();
    this.http.get<any[]>(`${environment.apiUrl}/api/ats/user/${user.id}`).subscribe({
      next: (data) => {
        this.atsMatches = data.map(match => ({
          ...match,
          matchDetails: JSON.parse(match.matchDetails || '{}')
        }));
        this.atsLoading = false;
      },
      error: (err) => {
        console.error('Error loading ATS matches:', err);
        const errorMessage = err.error?.message || 
          (err.status === 403 ? 'Access denied: You may not have permission to view these ATS matches.' : 
          'Failed to load ATS match results. Please try again.');
        this.atsError = errorMessage;
        this.atsLoading = false;
      }
    });
  }

  closeAtsModal(): void {
    this.selectedUser = null;
    this.atsMatches = [];
    this.atsError = null;
  }

  private loadUsers(): void {
    this.loading = true;
    this.error = null;
    this.http.get<any[]>(`${environment.apiUrl}/users`).subscribe({
      next: (data) => {
        this.users = data;
        this.loadPostNames();
        this.loadProgressForUsers();
        this.loadAtsScoresForUsers();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading users:', err);
        this.error = 'Failed to load users. Please check your connection and try again.';
        this.loading = false;
      }
    });
  }

  private loadPostNames(): void {
    const uniquePostIds = [...new Set(this.users.map(user => user.postId).filter(id => id))];
    if (uniquePostIds.length === 0) return;

    this.postService.getAllPosts().subscribe({
      next: (posts: any[]) => {
        posts.forEach(post => {
          this.postMap.set(post.id, post.name || post.title || 'Unnamed Post');
        });
        this.users.forEach(user => {
          user.postName = this.postMap.get(user.postId) || 'N/A';
        });
      },
      error: (err) => {
        console.error('Error loading posts:', err);
        this.users.forEach(user => {
          user.postName = 'N/A';
        });
      }
    });
  }

  private loadProgressForUsers(): void {
    const progressObservables = this.users.map(user =>
      this.http.get(`${environment.apiUrl}/api/progress/user/${user.id}`).pipe(
        catchError(err => {
          console.error(`Error loading progress for user ${user.id}:`, err);
          return of({ stage: 'N/A', percentage: 0 });
        })
      )
    );

    if (progressObservables.length > 0) {
      forkJoin(progressObservables).subscribe({
        next: (progresses) => {
          this.users.forEach((user, index) => {
            user.progress = progresses[index];
          });
        },
        error: (err) => {
          console.error('Error loading progress:', err);
        }
      });
    }
  }

  private loadAtsScoresForUsers(): void {
    const atsObservables = this.users.map(user =>
      this.http.get<any[]>(`${environment.apiUrl}/api/ats/user/${user.id}`).pipe(
        catchError(err => {
          console.error(`Error loading ATS score for user ${user.id}:`, err);
          return of([]);
        })
      )
    );

    if (atsObservables.length > 0) {
      forkJoin(atsObservables).subscribe({
        next: (atsResults) => {
          this.users.forEach((user, index) => {
            const matches = atsResults[index];
            // Use the first match's score or average if multiple matches exist
            user.matchScore = matches.length > 0 ? 
              matches.reduce((sum, match) => sum + (match.matchScore || 0), 0) / matches.length : null;
          });
        },
        error: (err) => {
          console.error('Error loading ATS scores:', err);
          this.users.forEach(user => {
            user.matchScore = null;
          });
        }
      });
    }
  }

  get filteredAndSortedUsers(): any[] {
    let filtered = [...this.users];

    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(user =>
        user.firstName?.toLowerCase().includes(term) ||
        user.lastName?.toLowerCase().includes(term) ||
        user.email?.toLowerCase().includes(term)
      );
    }

    if (this.sortConfig.active) {
      filtered.sort((a, b) => {
        let valA = this.getValue(a, this.sortConfig.active);
        let valB = this.getValue(b, this.sortConfig.active);

        if (valA == null) valA = '';
        if (valB == null) valB = '';

        if (this.sortConfig.active === 'role') {
          valA = a.role?.name || '';
          valB = b.role?.name || '';
        }

        if (this.sortConfig.active === 'authorities') {
          valA = (a.authorities && a.authorities[0]) ? a.authorities[0].authority : '';
          valB = (b.authorities && b.authorities[0]) ? b.authorities[0].authority : '';
        }

        if (this.sortConfig.active === 'postName') {
          valA = a.postName || '';
          valB = b.postName || '';
        }

        if (this.sortConfig.active === 'enabled') {
          valA = a.enabled ? 1 : 0;
          valB = b.enabled ? 1 : 0;
        }

        if (this.sortConfig.active === 'progress') {
          valA = a.progress?.stage || '';
          valB = b.progress?.stage || '';
        }

        if (this.sortConfig.active === 'matchScore') {
          valA = a.matchScore || 0;
          valB = b.matchScore || 0;
        }

        if (typeof valA === 'string') {
          valA = valA.toLowerCase();
          valB = valB.toLowerCase();
        }

        if (valA < valB) {
          return this.sortConfig.direction === 'asc' ? -1 : 1;
        }
        if (valA > valB) {
          return this.sortConfig.direction === 'asc' ? 1 : -1;
        }
        return 0;
      });
    }

    return filtered;
  }

  private getValue(obj: any, prop: string): any {
    return prop.split('.').reduce((o, p) => o?.[p], obj);
  }

  sort(column: string): void {
    if (this.sortConfig.active === column) {
      this.sortConfig.direction = this.sortConfig.direction === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortConfig.active = column;
      this.sortConfig.direction = 'asc';
    }
  }

  trackByUserId(index: number, user: any): any {
    return user.id;
  }
}
