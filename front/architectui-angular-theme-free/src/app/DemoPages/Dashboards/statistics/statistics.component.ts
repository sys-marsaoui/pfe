import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from 'src/environments/environment';
import { TokenDecoder } from 'src/app/shared/utils/jwt.util';

interface CandidateStats {
  candidateId: string;
  fullName: string;
  email: string;
  postTitle: string;
  status: string;
  quizScore: number;
  totalQuizScore: number;
  quizPercentage: number;
  atsScore: number;
  currentStage: string;
  appliedAt: string;
  cvValidated: boolean;
}

@Component({
  selector: 'app-statistics',
  templateUrl: './statistics.component.html',
  styleUrls: ['./statistics.component.scss']
})
export class StatisticsComponent implements OnInit {
  private readonly baseUrl = `${environment.apiUrl}/api/stats`;
  kpis: any = { total: 0, accepted: 0, rejected: 0, pending: 0, acceptanceRate: 0 };
  funnelData: any[] = [];
  distributionData: any[] = [];
  jobStats: any[] = [];
  candidates: CandidateStats[] = [];
  posts: any[] = [];
  selectedPostId = '';
  selectedStatus = '';
  searchTerm = '';
  loading = true;
  error = '';

  readonly statusOptions = ['InProgress', 'INVITED', 'PASSED', 'FAILED', 'INTERVIEW_SCHEDULED', 'ACCEPTED', 'REJECTED'];

  constructor(private http: HttpClient, private router: Router, private tokenDecoder: TokenDecoder) {}

  ngOnInit(): void {
    if (this.tokenDecoder.getUser().role !== 'ADMIN') {
      this.router.navigate(['/profile']);
      return;
    }
    this.http.get<any[]>(`${environment.apiUrl}/api/posts`).subscribe({
      next: (posts) => this.posts = posts || [],
      error: () => this.error = 'Impossible de charger les offres.'
    });
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.error = '';
    const postIds = this.selectedPostId ? [this.selectedPostId] : null;
    const statuses = this.selectedStatus ? [this.selectedStatus] : null;
    const filter = { postIds, statuses, fromDate: null, toDate: null, levels: null };

    this.http.post<any>(`${this.baseUrl}/kpis`, filter).subscribe({
      next: (data) => this.kpis = data,
      error: () => this.error = 'Impossible de charger les indicateurs.'
    });
    this.http.post<any[]>(`${this.baseUrl}/funnel`, filter).subscribe({
      next: (data) => this.funnelData = data || [],
      error: () => this.error = 'Impossible de charger le parcours de recrutement.'
    });
    this.http.post<any[]>(`${this.baseUrl}/distribution`, filter).subscribe({
      next: (data) => this.distributionData = data || [],
      error: () => this.error = 'Impossible de charger la répartition des scores.'
    });
    this.http.post<any[]>(`${this.baseUrl}/jobs`, filter).subscribe({
      next: (data) => this.jobStats = data || [],
      error: () => this.error = 'Impossible de charger les statistiques par offre.'
    });
    this.http.post<CandidateStats[]>(`${this.baseUrl}/candidates`, filter).subscribe({
      next: (data) => {
        this.candidates = data || [];
        this.loading = false;
      },
      error: () => {
        this.candidates = [];
        this.loading = false;
        this.error = 'Impossible de charger le suivi des candidats. Vérifiez les droits administrateur.';
      }
    });
  }

  clearFilters(): void {
    this.selectedPostId = '';
    this.selectedStatus = '';
    this.searchTerm = '';
    this.loadDashboard();
  }

  get filteredCandidates(): CandidateStats[] {
    const needle = this.searchTerm.trim().toLocaleLowerCase();
    if (!needle) return this.candidates;
    return this.candidates.filter(candidate =>
      `${candidate.fullName} ${candidate.email} ${candidate.postTitle}`.toLocaleLowerCase().includes(needle));
  }

  openCandidate(candidate: CandidateStats): void {
    this.router.navigate(['/users', candidate.candidateId]);
  }

  stageLabel(stage: string): string {
    const labels: Record<string, string> = {
      REGISTERED: 'Inscrit', CV_UPLOADED: 'CV reçu', CV_VERIFIED: 'CV vérifié',
      QUIZ_ACCEPTED: 'Quiz invité', QUIZ_PASSED: 'Quiz réussi', ENTRETIEN: 'Entretien'
    };
    return labels[stage] || stage || '—';
  }

  percent(value: number): number {
    return Number.isFinite(value) ? Math.max(0, Math.min(100, value)) : 0;
  }
}
