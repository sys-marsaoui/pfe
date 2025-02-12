import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';
import { QuizService } from 'src/app/core/services/QuizService';
import { ToastService } from 'src/app/core/services/ToasetService'; // Si tu utilises un service de notifications
import { TokenDecoder } from 'src/app/shared/utils/jwt.util';

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
  postId:string;
  user: any = {};
  constructor(private tokenDecoder: TokenDecoder, private route: ActivatedRoute, private router: Router, private quizService: QuizService, private toastService:ToastService ) {}

  ngOnInit(): void {
    // Get the quiz ID from the route
    this.quizId = this.route.snapshot.paramMap.get('id');
    if (this.quizId) {
      this.loadQuiz(this.quizId);
    }
     this.user = this.tokenDecoder.getUser();
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

  // Submit the quiz answers
  submitQuiz(): void {
    if (!this.quizId) return;

    const questionAnswers = [];
    Object.keys(this.answers)
    .forEach((key) => {
      questionAnswers.push({"questionId": key, "answerIds": this.answers[key] })
  });
  console.log(this.tokenDecoder.getUser());

    const payload = {
      quizId: this.quizId,
      postId: this.user.postId,
      userId: this.user.id,
      questionAnswers: questionAnswers
    };

    this.quizService.submitQuizAnswers(this.quizId, payload).subscribe(
      (response: any) => {
        this.toastService.success(`Your score: ${response.scoreDescription}`);
        console.log(jwtDecode(localStorage.getItem('auth_token'))['user']?.postId)
        this.router.navigateByUrl('detaille-poste/'+ payload.postId);
      },
      (error: any) => {
        console.error('Error submitting quiz:', error);
        this.toastService.error('Error submitting quiz');
      }
    );
  }
}