import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CreateQuizComponent } from '../create-quiz/create-quiz.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { QuizService } from 'src/app/core/services/QuizService';
import { PostService } from 'src/app/core/services/post.service';
import { TechnologyService } from 'src/app/core/services/TechnologyService';
import { Quizz, Technology } from 'src/app/core/models/quizz.model';

@Component({
  selector: 'app-quiz-list',
  templateUrl: './quiz-list.component.html',
  styleUrls: ['./quiz-list.component.scss']
})
export class QuizListComponent implements OnInit {

  quizzes: Quizz[] = [];
  filteredQuizzes: Quizz[] = [];
  technologies: Technology[] = [];
  
  // Filtres
  selectedTechnologies: string[] = [];
  selectedLevel: string = '';

  constructor(
    private router: Router,
    private quizService: QuizService, 
    private postService: PostService,
    private technologyService: TechnologyService,
    private modalService: NgbModal
  ) {}

  ngOnInit(): void {
    this.loadQuizzes();
    this.loadTechnologies();
  }

  // Charger tous les quiz
  loadQuizzes(): void {
    this.quizService.getAllQuizzes().subscribe(
      (data) => {
        this.quizzes = data;
        this.filteredQuizzes = data;
        this.applyFilters();
      },
      (error) => {
        console.error('Erreur lors du chargement des quiz:', error);
      }
    );
  }

  // Charger les technologies
  loadTechnologies(): void {
    this.technologyService.getAll().subscribe(
      (data) => {
        this.technologies = data;
      },
      (error) => {
        console.error('Erreur lors du chargement des technologies:', error);
      }
    );
  }

  // Appliquer les filtres
  applyFilters(): void {
    this.filteredQuizzes = this.quizzes.filter(quiz => {
      // Filtre par niveau
      const matchesLevel = !this.selectedLevel || quiz.level === this.selectedLevel;
      
      // Filtre par technologies
      let matchesTechnology = true;
      if (this.selectedTechnologies.length > 0) {
        const quizTechIds = this.extractQuizTechnologies(quiz).map(tech => tech?.id);
        matchesTechnology = this.selectedTechnologies.some(selectedTechId => 
          quizTechIds.includes(selectedTechId)
        );
      }
      
      return matchesLevel && matchesTechnology;
    });
  }

  // Réinitialiser les filtres
  resetFilters(): void {
    this.selectedTechnologies = [];
    this.selectedLevel = '';
    this.applyFilters();
  }

  // Supprimer un quiz
  deleteQuiz(id: string): void {
    if (confirm('Voulez-vous vraiment supprimer ce quiz ?')) {
      this.quizService.deleteQuiz(id).subscribe(
        () => {
          this.quizzes = this.quizzes.filter((quiz) => quiz.id !== id);
          this.applyFilters();
          console.log('Quiz supprimé avec succès');
        },
        (error) => {
          console.error('Erreur lors de la suppression du quiz:', error);
        }
      );
    }
  }

  // Ouvrir le modal pour créer un quiz
  openCreateQuizModal(): void {
    const modalRef = this.modalService.open(CreateQuizComponent, { 
      size: 'lg', 
      backdrop: 'static' 
    });
    
    modalRef.result.then(
      (result) => {
        if (result) {
          this.loadQuizzes();
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

  // Calculer le temps total du quiz
  calculateQuizTime(quiz: Quizz): number {
    if (!quiz?.questions || quiz.questions.length === 0) {
      return 0;
    }
    return quiz.questions
      .map(question => question.time || 0)
      .reduce((t1, t2) => t1 + t2, 0); // Ajout de la valeur initiale 0
  }

  // Extraire les technologies uniques d'un quiz
  extractQuizTechnologies(quiz: Quizz): Technology[] {
    if (!quiz?.questions) {
      return [];
    }
    
    // Extraire les technologies et éliminer les doublons
    const techMap = new Map<string, Technology>();
    quiz.questions.forEach(question => {
      if (question?.technology?.id) {
        techMap.set(question.technology.id, question.technology);
      }
    });
    
    return Array.from(techMap.values());
  }

  // Obtenir la classe CSS pour le badge de niveau
  getLevelBadgeClass(level: string): string {
    const levelClasses: { [key: string]: string } = {
      'JUNIOR': 'bg-success',
      'CONFIRMED': 'bg-info',
      'SENIOR': 'bg-warning',
      'EXPERT': 'bg-danger'
    };
    return levelClasses[level] || 'bg-secondary';
  }

  // Naviguer vers les détails du quiz
  viewQuizDetails(quizId: string): void {
    this.router.navigate(['/quizzes', quizId]);
  }
}