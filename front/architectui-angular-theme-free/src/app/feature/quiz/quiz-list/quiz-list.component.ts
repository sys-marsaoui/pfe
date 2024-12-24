import { Component, OnInit } from '@angular/core';
import { CreateQuizComponent } from '../create-quiz/create-quiz.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { QuizService } from 'src/app/core/services/QuizService';
import { PostService } from 'src/app/core/services/post.service';

@Component({
  selector: 'app-quiz-list',
  templateUrl: './quiz-list.component.html',
  styleUrls: ['./quiz-list.component.sass']
})
export class QuizListComponent implements OnInit {

  quizzes: any[] = [];
  selectedQuizId: string | null = null;

  constructor(private quizService: QuizService,  private postService: PostService, private modalService: NgbModal) {}

  ngOnInit(): void {
    this.loadQuizzes();
  }

  // Charger tous les quiz
  loadQuizzes(): void {
    this.quizService.getAllQuizzes().subscribe(
      (data) => {
        this.quizzes = data;
      },
      (error) => {
        console.error('Erreur lors du chargement des quiz:', error);
      }
    );
  }

  // Supprimer un quiz
  deleteQuiz(id: string): void {
    if (confirm('Voulez-vous vraiment supprimer ce quiz ?')) {
      this.quizService.deleteQuiz(id).subscribe(
        () => {
          this.quizzes = this.quizzes.filter((quiz) => quiz.id !== id);
        },
        (error) => {
          console.error('Erreur lors de la suppression du quiz:', error);
        }
      );
    }
  }

  // Ouvrir le modal pour créer un quiz
  openCreateQuizModal(): void {
    const modalRef = this.modalService.open(CreateQuizComponent, { size: 'lg', backdrop: 'static' });
    modalRef.result.then(
      (result) => {
        if (result) {
          this.loadQuizzes(); // Recharger les quiz après la création
        }
      },
      () => {
        console.log('Modal fermé sans action.');
      }
    );
  }

  // Associer un quiz à un poste
  associateQuizToPost(quizId: string, postId: string): void {
    const payload = { quizId };
    this.postService.updatePost(postId, payload).subscribe(
      () => {
        console.log('Quiz associé au poste avec succès.');
      },
      (error) => {
        console.error('Erreur lors de l\'association du quiz:', error);
      }
    );
  }
}
