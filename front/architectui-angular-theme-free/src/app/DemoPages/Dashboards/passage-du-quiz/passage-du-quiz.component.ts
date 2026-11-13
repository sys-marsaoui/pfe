import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { interval, Subscription } from 'rxjs';
import { QuizService } from 'src/app/core/services/QuizService';
import { ToastService } from 'src/app/core/services/ToasetService';
import { TokenDecoder } from 'src/app/shared/utils/jwt.util';

interface QuizOption {
  id: string;
  text: string;
  correct: boolean;
}

interface Question {
  id: string;
  text: string;
  options: QuizOption[];
  points: number;
  time: number;
  technology: string;
}

interface Quiz {
  id: string;
  title: string;
  questions: Question[];
  level: string;
}

type QuestionStatus = 'pending' | 'active' | 'answered' | 'expired';

@Component({
  selector: 'passage-du-quiz',
  templateUrl: './passage-du-quiz.component.html',
  styleUrls: ['./passage-du-quiz.component.scss']
})
export class PassageDuQuizComponent implements OnInit, OnDestroy {
  quiz: Quiz = { id: '', title: '', questions: [], level: '' };
  questions: Question[] = [];
  currentQuestionIndex: number = 0;
  answers: { [questionId: string]: string } = {};
  quizId: string | null = null;
  postId: string | null = null;
  user: any = {};

  // Timer & Status
  timeRemaining: number = 0;
  timerSubscription?: Subscription;
  questionTimers: number[] = [];           // Remaining time per question
  questionStatus: QuestionStatus[] = [];   // Lock status per question

  isQuizStarted: boolean = false;
  isQuizFinished: boolean = false;

  String = String;

  constructor(
    private tokenDecoder: TokenDecoder,
    private route: ActivatedRoute,
    private router: Router,
    private quizService: QuizService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.quizId = this.route.snapshot.paramMap.get('id');
    this.postId = this.route.snapshot.queryParamMap.get('postId');
    if (this.quizId) {
      this.loadQuiz(this.quizId, this.postId);
    }
    this.user = this.tokenDecoder.getUser();
  }

  ngOnDestroy(): void {
    this.stopTimer();
  }

  loadQuiz(id: string, postId: string | null): void {
    this.quizService.getQuizById(id, postId || undefined).subscribe(
      (data: any) => {
        this.quiz = data;
        this.questions = data.questions;

        // Initialize per-question timer & status
        this.questionTimers = this.questions.map(q => q.time);
        this.questionStatus = this.questions.map((_, i) => i === 0 ? 'active' : 'pending');
      },
      (error: any) => {
        console.error('Error fetching quiz:', error);
        this.toastService.error('Erreur lors du chargement du quiz');
      }
    );
  }

  startQuiz(): void {
    this.isQuizStarted = true;
    this.startTimer();
  }

  startTimer(): void {
    this.stopTimer();

    const idx = this.currentQuestionIndex;
    this.timeRemaining = this.questionTimers[idx];

    if (this.timeRemaining <= 0) {
      this.timeRemaining = 0;
      this.questionStatus[idx] = 'expired';
      return;
    }

    this.timerSubscription = interval(1000).subscribe(() => {
      this.questionTimers[idx]--;
      this.timeRemaining = this.questionTimers[idx];

      if (this.timeRemaining <= 0) {
        this.questionTimers[idx] = 0;
        this.timeRemaining = 0;
        this.questionStatus[idx] = 'expired';
        this.stopTimer();

        // Auto-advance after 1s
        setTimeout(() => this.nextQuestion(), 1000);
      }
    });
  }

  stopTimer(): void {
    if (this.timerSubscription) {
      this.timerSubscription.unsubscribe();
    }
  }

  formatTime(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }

  updateAnswer(questionIndex: number, optionId: string): void {
    const questionId = this.questions[questionIndex].id;
    this.answers[questionId] = optionId;
  }

  isAnswered(questionIndex: number): boolean {
    const questionId = this.questions[questionIndex].id;
    return !!this.answers[questionId];
  }

  nextQuestion(): void {
    this.stopTimer();

    const currentIdx = this.currentQuestionIndex;

    // Mark current as answered (even if time not expired)
    if (this.timeRemaining > 0 && this.isAnswered(currentIdx)) {
      this.questionStatus[currentIdx] = 'answered';
    } else if (this.timeRemaining <= 0) {
      this.questionStatus[currentIdx] = 'expired';
    }

    if (currentIdx < this.questions.length - 1) {
      this.currentQuestionIndex++;
      this.questionStatus[this.currentQuestionIndex] = 'active';
      this.startTimer();
    } else {
      this.submitQuiz();
    }
  }

  previousQuestion(): void {
    if (this.currentQuestionIndex <= 0) return;

    const prevIdx = this.currentQuestionIndex - 1;
    if (this.questionStatus[prevIdx] === 'expired' || this.questionStatus[prevIdx] === 'answered') {
      this.toastService.error('Vous ne pouvez pas revenir à cette question.');
      return;
    }

    this.stopTimer();
    this.currentQuestionIndex = prevIdx;
    this.questionStatus[this.currentQuestionIndex] = 'active';
    this.startTimer();
  }

  goToQuestion(index: number): void {
    if (index === this.currentQuestionIndex) return;

    const status = this.questionStatus[index];
    if (status === 'expired' || status === 'answered') {
      this.toastService.success('Vous ne pouvez plus accéder à cette question.');
      return;
    }

    this.stopTimer();
    this.currentQuestionIndex = index;
    this.questionStatus[index] = 'active';
    this.startTimer();
  }

  submitQuiz(): void {
    this.stopTimer();

    if (!this.quizId) {
      this.toastService.error('ID du quiz manquant');
      return;
    }

    const questionAnswers: any[] = Object.keys(this.answers).map(key => ({
      questionId: key,
      answerIds: this.answers[key]
    }));

    const payload = {
      quizId: this.quizId,
      postId: this.postId || this.user.postId,
      userId: this.user.id,
      questionAnswers
    };

    this.quizService.submitQuizAnswers(this.quizId, payload).subscribe(
      (response: any) => {
        this.isQuizFinished = true;
        this.toastService.success(`Votre score: ${response.scoreDescription}`);

        setTimeout(() => {
          this.router.navigateByUrl('profile');
        }, 3000);
      },
      (error: any) => {
        console.error('Error submitting quiz:', error);
        this.toastService.error('Erreur lors de la soumission du quiz');
      }
    );
  }

  getProgressPercentage(): number {
    return ((this.currentQuestionIndex + 1) / this.questions.length) * 100;
  }

  getSelectedAnswerForCurrentQuestion(): string | null {
    const questionId = this.questions[this.currentQuestionIndex]?.id;
    return this.answers[questionId] || null;
  }
}
