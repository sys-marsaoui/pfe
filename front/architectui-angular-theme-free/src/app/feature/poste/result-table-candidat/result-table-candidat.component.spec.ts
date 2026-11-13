import { CommonModule } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { NgbModal, NgbTimepickerModule } from '@ng-bootstrap/ng-bootstrap';
import { of } from 'rxjs';
import { CandidatureService } from 'src/app/core/services/candidature.service';
import { PostService } from 'src/app/core/services/post.service';
import { Candidature } from 'src/app/core/models/candidature.model';
import { ResultTableCandidatComponent } from './result-table-candidat.component';

describe('ResultTableCandidatComponent', () => {
  let component: ResultTableCandidatComponent;
  let fixture: ComponentFixture<ResultTableCandidatComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommonModule, FormsModule, RouterTestingModule, NgbTimepickerModule],
      declarations: [ResultTableCandidatComponent],
      providers: [
        { provide: CandidatureService, useValue: { getAllByPosts: () => of({}) } },
        { provide: PostService, useValue: { getPostById: () => of({}) } },
        { provide: NgbModal, useValue: { open: () => ({}), dismissAll: () => undefined } }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ResultTableCandidatComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('creates the candidates dashboard', () => {
    expect(component).toBeTruthy();
  });

  it('shows a passing quiz score as eligible for an interview', () => {
    const candidate = { status: 'PASSED', quizScore: 7, totalQuizScore: 10 } as Candidature;
    expect(component.canScheduleInterview(candidate)).toBeTrue();
    expect(component.getScoreClass(candidate.quizScore, candidate.totalQuizScore)).toBe('progress-high');
  });

  it('uses the email when the candidate name is missing', () => {
    const candidate = {
      userId: 42,
      user: { email: 'candidate@example.test' }
    } as Candidature;
    expect(component.getCandidateName(candidate)).toBe('candidate@example.test');
    expect(component.getCandidateInitials(candidate)).toBe('C');
  });
});
