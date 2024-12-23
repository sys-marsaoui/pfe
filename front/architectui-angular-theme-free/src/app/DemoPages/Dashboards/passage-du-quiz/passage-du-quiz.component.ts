import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { QuizService } from 'src/app/core/services/QuizService';
import { ToastService } from 'src/app/core/services/ToasetService'; // Si tu utilises un service de notifications

@Component({
  selector: 'passage-du-quiz',
  templateUrl: './passage-du-quiz.component.html',
  styleUrls: ['./passage-du-quiz.component.sass']
})
export class PassageDuQuizComponent implements OnInit {

  heading = 'RGI Tunis';
  subheading = 'We re Hiring! Come And Join Us To Build Your Dream.';
  icon = 'pe-7s-plane icon-gradient bg-tempting-azure';
  quiz: any = {};
  questions: any[] = [];
  answers: any = {};
  quizId: string | null = null;

  constructor(private route: ActivatedRoute, private quizService: QuizService,private toastService:ToastService) {}

  ngOnInit(): void {
    // Get the quiz ID from the route
    this.quizId = this.route.snapshot.paramMap.get('id');
    if (this.quizId) {
      this.loadQuiz(this.quizId);
    }else{
      this.quizId ="676925d921e3c36c489ba027"
      this.loadQuiz(this.quizId);
    }
  }

  // Load the quiz from the backend
  loadQuiz(id: string): void {
    this.quizService.getQuizById(id).subscribe(
      (data: any) => {
        this.quiz = data;
        this.questions = data.questions;
      },
      (error: any) => {
        console.error('Error fetching quiz:', error);
      }
    );
  }

  // Update answers for radio or text inputs
  updateAnswer(questionIndex: number, answerValue: any): void {
    const questionId = this.questions[questionIndex].id;
    this.answers[questionId] = answerValue;
  }

  // Update answers for checkboxes
  updateCheckboxAnswer(questionIndex: number, answerValue: any, event: any): void {
    const questionId = this.questions[questionIndex].id;
    if (!this.answers[questionId]) {
      this.answers[questionId] = [];
    }

    if (event.target.checked) {
      this.answers[questionId].push(answerValue);
    } else {
      this.answers[questionId] = this.answers[questionId].filter((val: any) => val !== answerValue);
    }
  }

  // Submit the quiz answers
  submitQuiz(): void {
    if (!this.quizId) return;

    const payload = {
      quizId: this.quizId,
      answers: this.answers,
    };

    this.quizService.submitQuizAnswers(this.quizId, payload).subscribe(
      (response: any) => {
        console.log('Quiz submitted successfully:', response);
        this.toastService.success(`Your score: ${response.userScore}/${response.totalScore}`);
      },
      (error: any) => {
        console.error('Error submitting quiz:', error);
        this.toastService.error('Error submitting quiz');
      }
    );
  }
}