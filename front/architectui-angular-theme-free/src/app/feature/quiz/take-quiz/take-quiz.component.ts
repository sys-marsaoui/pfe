import { Component, Input, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { QuizService } from 'src/app/core/services/QuizService';
import { Quizz } from 'src/app/core/models/quizz.model';

@Component({
  selector: 'app-take-quiz',
  templateUrl: './take-quiz.component.html',
  styleUrls: ['./take-quiz.component.sass']
})
export class TakeQuizComponent implements OnInit {
  @Input() quizId: string = '';
  @Input() postId: string = '';
  @Input() userId: string | null = null;
  quiz: Quizz | null = null;
  quizForm: FormGroup;
  loading: boolean = true;
  error: string = '';

  constructor(
    private fb: FormBuilder,
    private quizService: QuizService,
    public activeModal: NgbActiveModal
  ) {
    this.quizForm = this.fb.group({
      questionAnswers: this.fb.array([])
    });
  }

  ngOnInit(): void {
    this.loadQuiz();
  }

  get questionAnswers(): FormArray {
    return this.quizForm.get('questionAnswers') as FormArray;
  }

  loadQuiz(): void {
    this.quizService.getQuizById(this.quizId).subscribe({
      next: (quiz) => {
        this.quiz = quiz;
        this.quiz.questions.forEach(() => {
          this.questionAnswers.push(this.fb.control('', Validators.required));
        });
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Erreur lors du chargement du quiz.';
        console.error('Erreur:', error);
        this.loading = false;
      }
    });
  }

  submitQuiz(): void {
    if (this.quizForm.valid && this.quiz && this.userId && this.postId) {
      const payload = {
        quizId: this.quizId,
        postId: this.postId,
        userId: parseInt(this.userId),
        questionAnswers: this.quiz.questions.map((q, i) => ({
          questionId: q.id,
          answerIds: this.questionAnswers.at(i).value
        }))
      };
      this.quizService.submitQuizAnswers(this.quizId, payload).subscribe({
        next: (response) => {
          console.log('Quiz soumis:', response);
          this.activeModal.close(response);
        },
        error: (error) => {
          this.error = 'Erreur lors de la soumission du quiz.';
          console.error('Erreur:', error);
        }
      });
    } else {
      this.error = 'Formulaire invalide ou informations manquantes.';
    }
  }

  closeModal(): void {
    this.activeModal.dismiss();
  }
}