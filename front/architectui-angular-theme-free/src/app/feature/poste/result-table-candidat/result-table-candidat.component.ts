import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef, Input } from '@angular/core';
import { NgbModal, NgbDateStruct, NgbTimeStruct } from '@ng-bootstrap/ng-bootstrap';
import { Candidature } from 'src/app/core/models/candidature.model';
import { CandidatureService } from 'src/app/core/services/candidature.service';
import { PostService } from 'src/app/core/services/post.service';
import { finalize } from 'rxjs/operators';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

interface PostWithCandidates {
  postId: string;
  postName: string;
  candidates: Candidature[];
}

@Component({
  selector: 'app-result-table-candidat',
  templateUrl: './result-table-candidat.component.html',
  styleUrls: ['./result-table-candidat.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ResultTableCandidatComponent implements OnInit {
  @Input() postId?: string;
  postsWithCandidates: PostWithCandidates[] = [];
  loading = false;
  errorMsg?: string;

  // Modal
  selectedCandidat?: Candidature;
  interviewDate?: NgbDateStruct;
  interviewTime?: NgbTimeStruct;
  minDate!: NgbDateStruct;
  minDateInput = '';

  private postNameCache = new Map<string, string>();

  constructor(
    private modalService: NgbModal,
    private candService: CandidatureService,
    private postService: PostService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.setMinDate();
    this.loadAllPostsCandidates();
  }

  private setMinDate(): void {
    const today = new Date();
    this.minDate = {
      year: today.getFullYear(),
      month: today.getMonth() + 1,
      day: today.getDate()
    };
    this.minDateInput = `${this.minDate.year}-${String(this.minDate.month).padStart(2, '0')}-${String(this.minDate.day).padStart(2, '0')}`;
  }

  isWeekend(date: NgbDateStruct): boolean {
    const d = new Date(date.year, date.month - 1, date.day);
    return d.getDay() === 0 || d.getDay() === 6;
  }

  canScheduleInterview(candidate: Candidature): boolean {
    return candidate.status === 'PASSED' && candidate.totalQuizScore > 0 &&
      candidate.quizScore / candidate.totalQuizScore >= 0.7;
  }

  getCandidateName(candidate: Candidature): string {
    const firstName = candidate.user?.firstName?.trim();
    const lastName = candidate.user?.lastName?.trim();
    const fullName = [firstName, lastName].filter(Boolean).join(' ');
    return fullName || candidate.user?.email || `Candidat #${candidate.userId}`;
  }

  getCandidateInitials(candidate: Candidature): string {
    const firstName = candidate.user?.firstName?.trim();
    const lastName = candidate.user?.lastName?.trim();
    if (firstName || lastName) {
      return `${firstName?.charAt(0) || ''}${lastName?.charAt(0) || ''}`.toUpperCase();
    }
    return (candidate.user?.email?.charAt(0) || 'C').toUpperCase();
  }

  viewCandidate(candidate: Candidature): void {
    if (candidate.userId) {
      this.router.navigate(['/users', candidate.userId]);
    }
  }

  getPostName(postId?: string): string {
    return (postId && this.postNameCache.get(postId)) || 'Offre introuvable';
  }

  getScoreClass(score?: number, total?: number): string {
    if (!total || total <= 0) return 'progress-low';
    const percentage = ((score || 0) / total) * 100;
    if (percentage >= 70) return 'progress-high';
    if (percentage >= 50) return 'progress-medium';
    return 'progress-low';
  }

  onInterviewDateChange(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    if (!value) {
      this.interviewDate = undefined;
      return;
    }
    const [year, month, day] = value.split('-').map(Number);
    const selectedDate = { year, month, day };
    if (this.isWeekend(selectedDate)) {
      this.interviewDate = undefined;
      alert('Choisissez un jour ouvrable pour l’entretien.');
      return;
    }
    this.interviewDate = selectedDate;
  }

  formatInterviewDate(): string {
    if (!this.interviewDate) return '';
    const { year, month, day } = this.interviewDate;
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
  }

  // ==================================================================
  // LOAD ALL POSTS + CANDIDATES + POST NAMES
  // ==================================================================
  loadAllPostsCandidates(): void {
    this.loading = true;
    this.errorMsg = undefined;
    this.postsWithCandidates = [];

    this.candService.getAllByPosts()
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: (data) => {
          const entries = Object.entries(data).filter(([id]) => !this.postId || id === this.postId);
          if (entries.length === 0) {
            this.cdr.markForCheck();
            return;
          }

          const postIds = entries.map(([id]) => id);

          const postRequests = postIds.map(id =>
            this.postService.getPostById(id).pipe(
              catchError(err => {
                console.warn(`Failed to load post ${id}:`, err);
                return of({ id, name: 'Unknown Post', title: 'Unknown' });
              })
            )
          );

          forkJoin(postRequests).subscribe({
            next: (posts) => {
              posts.forEach(post => {
                const name = post.name || post.title || 'Untitled Post';
                this.postNameCache.set(post.id, name);
              });

              this.postsWithCandidates = entries.map(([postId, candidates]) => {
                const name = this.postNameCache.get(postId) || 'Loading...';
                return { postId, postName: name, candidates };
              });

              this.cdr.markForCheck();
            },
            error: (err) => {
              this.errorMsg = 'Failed to load post names.';
              console.error(err);
              this.cdr.markForCheck();
            }
          });
        },
        error: (err) => {
          this.errorMsg = err.message || 'Failed to load candidates';
          console.error(err);
          this.cdr.markForCheck();
        }
      });
  }

  // ==================================================================
  // MODAL & ACTIONS
  // ==================================================================
  openAcceptModal(content: any, cand: Candidature) {
    this.selectedCandidat = cand;
    this.interviewDate = undefined;
    this.interviewTime = { hour: 9, minute: 0, second: 0 };
    this.modalService.open(content, { ariaLabelledBy: 'acceptModalLabel', size: 'md' });
  }

  confirmAccept() {
    if (!this.selectedCandidat || !this.interviewDate || !this.interviewTime) return;

    const isoDateTime = this.buildIsoDateTime(this.interviewDate, this.interviewTime);

    this.candService.accept(this.selectedCandidat.id, isoDateTime).subscribe({
      next: (updated) => {
        this.updateLocalList(updated);
        this.modalService.dismissAll();
        this.cdr.markForCheck();
      },
      error: (err) => {
        alert(`Error: ${err.message}`);
        this.cdr.markForCheck();
      }
    });
  }

  reject(cand: Candidature) {
    if (!confirm(`Reject ${cand.user.firstName} ${cand.user.lastName}?`)) return;

    this.candService.reject(cand.id).subscribe({
      next: (updated) => {
        this.updateLocalList(updated);
        this.cdr.markForCheck();
      },
      error: (err) => {
        alert(`Error: ${err.message}`);
        this.cdr.markForCheck();
      }
    });
  }

  private updateLocalList(updated: Candidature) {
    this.postsWithCandidates.forEach(post => {
      const idx = post.candidates.findIndex(c => c.id === updated.id);
      if (idx > -1) post.candidates[idx] = updated;
    });
    this.cdr.markForCheck();
  }

  private buildIsoDateTime(date: NgbDateStruct, time: NgbTimeStruct): string {
    const pad = (value: number) => String(value).padStart(2, '0');
    return `${date.year}-${pad(date.month)}-${pad(date.day)}T${pad(time.hour)}:${pad(time.minute)}:00`;
  }
}
