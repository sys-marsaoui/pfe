import { Component, OnInit, ViewChild, TemplateRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { QuizService } from 'src/app/core/services/QuizService';
import { PostService } from 'src/app/core/services/post.service';
import { Location } from '@angular/common';

interface QuizOption {
  id: string;
  text: string;
  correct: boolean;
}

interface QuizQuestion {
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
  level: string;
  questions: QuizQuestion[];
}

interface Post {
  id: string;
  name?: string;
  title?: string;
}

@Component({
  selector: 'app-quiz-details',
  templateUrl: './quiz-details.component.html',
  styleUrls: ['./quiz-details.component.scss']
})
export class QuizDetailsComponent implements OnInit {

  @ViewChild('assignPostModal') assignPostModal!: TemplateRef<any>;

  quiz: Quiz | null = null;
  quizId: string = '';
  isEditMode: boolean = false;
  loading: boolean = false;
  editForm!: FormGroup;
  selectedPostId: string = '';
  
  // Posts data
  posts: Post[] = [];
  postsLoading: boolean = false;
  postsError: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private location: Location,
    private fb: FormBuilder,
    private quizService: QuizService,
    private postService: PostService,
    private modalService: NgbModal
  ) {
    this.initializeEditForm();
  }

  ngOnInit(): void {
    this.quizId = this.route.snapshot.params['id'];
    if (this.quizId) {
      this.loadQuizDetails();
    }
  }

  /**
   * Initialize the edit form
   */
  private initializeEditForm(): void {
    this.editForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3)]],
      level: ['', Validators.required],
      questions: this.fb.array([])
    });
  }

  /**
   * Load quiz details from API
   */
  loadQuizDetails(): void {
    this.loading = true;
    this.quizService.getQuizById(this.quizId).subscribe(
      (data: Quiz) => {
        this.quiz = data;
        this.loading = false;
        console.log('Quiz loaded:', data);
      },
      (error) => {
        console.error('Error loading quiz:', error);
        this.loading = false;
        alert('Erreur lors du chargement du quiz');
      }
    );
  }

  /**
   * Load all posts
   */
  loadPosts(): void {
    this.postsLoading = true;
    this.postsError = false;
    
    this.postService.getAllPosts().subscribe(
      (data: Post[]) => {
        this.posts = data;
        this.postsLoading = false;
        console.log('Posts loaded:', data);
      },
      (error) => {
        console.error('Error loading posts:', error);
        this.postsLoading = false;
        this.postsError = true;
      }
    );
  }

  /**
   * Toggle edit mode
   */
  toggleEditMode(): void {
    if (this.isEditMode) {
      this.isEditMode = false;
    } else {
      this.isEditMode = true;
      this.populateEditForm();
    }
  }

  /**
   * Populate edit form with current quiz data
   */
  private populateEditForm(): void {
    if (!this.quiz) return;

    this.editForm.patchValue({
      title: this.quiz.title,
      level: this.quiz.level
    });

    // Clear existing questions
    this.editQuestions.clear();

    // Add questions to form
    this.quiz.questions.forEach(question => {
      const questionGroup = this.fb.group({
        id: [question.id],
        text: [question.text, [Validators.required, Validators.minLength(10)]],
        time: [question.time, [Validators.required, Validators.min(15)]],
        points: [question.points, [Validators.required, Validators.min(1)]],
        technology: [question.technology || ''],
        options: this.fb.array([])
      });

      // Add options to question
      question.options.forEach(option => {
        const optionGroup = this.fb.group({
          id: [option.id],
          text: [option.text, Validators.required],
          correct: [option.correct]
        });
        (questionGroup.get('options') as FormArray).push(optionGroup);
      });

      this.editQuestions.push(questionGroup);
    });
  }

  /**
   * Get edit questions FormArray
   */
  get editQuestions(): FormArray {
    return this.editForm.get('questions') as FormArray;
  }

  /**
   * Get options for a specific question in edit mode
   */
  getEditOptions(questionIndex: number): FormArray {
    return this.editQuestions.at(questionIndex).get('options') as FormArray;
  }

  /**
   * Add a new question in edit mode
   */
  addNewQuestion(): void {
    const questionGroup = this.fb.group({
      id: [''], // Empty ID for new question
      text: ['', [Validators.required, Validators.minLength(10)]],
      time: [30, [Validators.required, Validators.min(15)]],
      points: [10, [Validators.required, Validators.min(1)]],
      technology: [''],
      options: this.fb.array([])
    });

    // Add two default options
    this.addOptionToQuestion(questionGroup);
    this.addOptionToQuestion(questionGroup);

    this.editQuestions.push(questionGroup);
  }

  /**
   * Add option to a specific question group
   */
  private addOptionToQuestion(questionGroup: FormGroup): void {
    const options = questionGroup.get('options') as FormArray;
    const optionGroup = this.fb.group({
      id: [''],
      text: ['', Validators.required],
      correct: [false]
    });
    options.push(optionGroup);
  }

  /**
   * Add option in edit mode
   */
  addOption(questionIndex: number): void {
    const options = this.getEditOptions(questionIndex);
    const optionGroup = this.fb.group({
      id: [''],
      text: ['', Validators.required],
      correct: [false]
    });
    options.push(optionGroup);
  }

  /**
   * Remove option in edit mode
   */
  removeOption(questionIndex: number, optionIndex: number): void {
    const options = this.getEditOptions(questionIndex);
    if (options.length > 2) {
      options.removeAt(optionIndex);
    }
  }

  /**
   * Remove question in edit mode
   */
  removeQuestion(index: number): void {
    if (this.editQuestions.length > 1) {
      if (confirm('Êtes-vous sûr de vouloir supprimer cette question ?')) {
        this.editQuestions.removeAt(index);
      }
    }
  }

  /**
   * Save quiz modifications
   */
  saveQuiz(): void {
    if (this.editForm.invalid) {
      this.markFormGroupTouched(this.editForm);
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    const quizData = this.prepareQuizData();
    this.loading = true;

    this.quizService.updateQuiz(this.quizId, quizData).subscribe(
      (response) => {
        console.log('Quiz updated successfully:', response);
        this.loading = false;
        this.isEditMode = false;
        this.loadQuizDetails(); // Reload to get updated data
        alert('Quiz mis à jour avec succès!');
      },
      (error) => {
        console.error('Error updating quiz:', error);
        this.loading = false;
        alert('Erreur lors de la mise à jour: ' + (error.error?.message || error.message));
      }
    );
  }

  /**
   * Prepare quiz data for API
   */
  private prepareQuizData(): any {
    const formValue = this.editForm.value;
    
    return {
      title: formValue.title.trim(),
      level: formValue.level,
      questions: formValue.questions.map((question: any) => {
        const questionData: any = {
          text: question.text.trim(),
          points: Number(question.points),
          time: Number(question.time),
          technology: question.technology || '',
          options: question.options.map((option: any) => ({
            text: option.text.trim(),
            correct: Boolean(option.correct)
          }))
        };
        
        // Include ID only if it exists (for existing questions)
        if (question.id) {
          questionData.id = question.id;
        }
        
        return questionData;
      })
    };
  }

  /**
   * Cancel edit mode
   */
  cancelEdit(): void {
    if (confirm('Annuler les modifications ?')) {
      this.isEditMode = false;
      this.initializeEditForm();
    }
  }

  /**
   * Mark form group as touched
   */
  private markFormGroupTouched(formGroup: FormGroup | FormArray): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      if (control instanceof FormGroup || control instanceof FormArray) {
        this.markFormGroupTouched(control);
      } else {
        control?.markAsTouched();
      }
    });
  }

  /**
   * Open assign to post modal
   */
  assignToPost(): void {
    this.selectedPostId = '';
    this.loadPosts(); // Load posts when modal opens
    this.modalService.open(this.assignPostModal, { centered: true });
  }

  /**
   * Confirm assignment to post
   */
  confirmAssignToPost(modal: any): void {
    if (!this.selectedPostId || this.selectedPostId.trim() === '') {
      alert('Veuillez sélectionner un poste');
      return;
    }

    this.loading = true;
    const payload = {
      quizId: this.quizId,
      postId: this.selectedPostId.trim()
    };

    this.quizService.assignQuizToPost(payload).subscribe(
      (response) => {
        console.log('Quiz assigned successfully:', response);
        this.loading = false;
        modal.close();
        alert('Quiz assigné au poste avec succès!');
      },
      (error) => {
        console.error('Error assigning quiz:', error);
        this.loading = false;
        alert('Erreur lors de l\'assignation: ' + (error.error?.message || error.message));
      }
    );
  }

  /**
   * Calculate total time
   */
  calculateTotalTime(): number {
    if (!this.quiz?.questions) return 0;
    return this.quiz.questions.reduce((total, q) => total + (q.time || 0), 0);
  }

  /**
   * Calculate total points
   */
  calculateTotalPoints(): number {
    if (!this.quiz?.questions) return 0;
    return this.quiz.questions.reduce((total, q) => total + (q.points || 0), 0);
  }

  /**
   * Extract unique technologies
   */
  extractUniqueTechnologies(): string[] {
    if (!this.quiz?.questions) return [];
    const technologies = this.quiz.questions
      .map(q => q.technology)
      .filter(tech => tech && tech.trim() !== '');
    return Array.from(new Set(technologies));
  }

  /**
   * Get level badge class
   */
  getLevelBadgeClass(level: string): string {
    const levelClasses: { [key: string]: string } = {
      'JUNIOR': 'bg-success',
      'CONFIRMED': 'bg-info',
      'SENIOR': 'bg-warning',
      'EXPERT': 'bg-danger'
    };
    return levelClasses[level] || 'bg-secondary';
  }

  /**
   * Go back to previous page
   */
  goBack(): void {
    this.location.back();
  }
}