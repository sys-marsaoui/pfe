import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { QuizService } from 'src/app/core/services/QuizService';
import { TechnologyService } from 'src/app/core/services/TechnologyService';

@Component({
  selector: 'app-create-quiz',
  templateUrl: './create-quiz.component.html',
  styleUrls: ['./create-quiz.component.scss']
})
export class CreateQuizComponent implements OnInit {

  quizForm: FormGroup;
  technologies: any[] = [];

  constructor(
    private fb: FormBuilder,
    private quizService: QuizService,
    private technologyService: TechnologyService,
    public activeModal: NgbActiveModal
  ) {
    this.quizForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3)]],
      level: ['', Validators.required],
      questions: this.fb.array([]),
    });
    // Add the first question by default
    this.addQuestion();
  }

  ngOnInit(): void {
    this.loadTechnologies();
  }

  get questions(): FormArray {
    return this.quizForm.get('questions') as FormArray;
  }

  /**
   * Add a new question to the quiz
   */
  addQuestion(): void {
    const questionGroup = this.fb.group({
      text: ['', [Validators.required, Validators.minLength(10)]],
      points: [10, [Validators.required, Validators.min(1), Validators.max(100)]],
      time: [30, [Validators.required, Validators.min(15), Validators.max(300)]],
      technology: ['', Validators.required],
      options: this.fb.array([], this.minOptionsValidator(2)),
    });
    
    this.questions.push(questionGroup);
    
    // Add two default options for each new question
    const newIndex = this.questions.length - 1;
    this.addOption(newIndex);
    this.addOption(newIndex);
  }

  /**
   * Remove a question from the quiz
   */
  removeQuestion(index: number): void {
    if (this.questions.length > 1) {
      if (confirm('Êtes-vous sûr de vouloir supprimer cette question ?')) {
        this.questions.removeAt(index);
      }
    }
  }

  /**
   * Get options array for a specific question
   */
  getOptions(questionIndex: number): FormArray {
    return this.questions.at(questionIndex).get('options') as FormArray;
  }

  /**
   * Add a new option to a question
   */
  addOption(questionIndex: number): void {
    const options = this.getOptions(questionIndex);
    const optionGroup = this.fb.group({
      text: ['', [Validators.required, Validators.minLength(1)]],
      isCorrect: [false],
    });
    options.push(optionGroup);
  }

  /**
   * Remove an option from a question
   */
  removeOption(questionIndex: number, optionIndex: number): void {
    const options = this.getOptions(questionIndex);
    if (options.length > 2) {
      options.removeAt(optionIndex);
    }
  }

  /**
   * Save the quiz
   */
  saveQuiz(): void {
    if (this.quizForm.invalid) {
      this.markFormGroupTouched(this.quizForm);
      alert('Veuillez remplir tous les champs obligatoires correctement.');
      return;
    }

    // Validate that each question has at least one correct answer
    const validationErrors = this.validateQuestions();
    if (validationErrors.length > 0) {
      alert('Erreurs de validation:\n' + validationErrors.join('\n'));
      return;
    }

    const quizData = this.prepareQuizData();
    console.log('Sending quiz data:', quizData);
    
    this.quizService.createQuiz(quizData).subscribe(
      (response) => {
        console.log('Quiz saved successfully:', response);
        this.activeModal.close(response);
      },
      (error) => {
        console.error('Error saving quiz:', error);
        const errorMessage = error.error?.message || error.message || 'Erreur inconnue';
        alert('Erreur lors de l\'enregistrement du quiz:\n' + errorMessage);
      }
    );
  }

  /**
   * Validate that each question has at least one correct answer
   */
  private validateQuestions(): string[] {
    const errors: string[] = [];
    
    this.questions.controls.forEach((question, index) => {
      const options = this.getOptions(index);
      const hasCorrectAnswer = options.controls.some(
        option => option.get('isCorrect')?.value === true
      );
      
      if (!hasCorrectAnswer) {
        errors.push(`Question ${index + 1}: Au moins une réponse correcte est requise`);
      }

      if (options.length < 2) {
        errors.push(`Question ${index + 1}: Au moins 2 options sont requises`);
      }
    });
    
    return errors;
  }

  /**
   * Prepare the data to match backend expectations
   */
  private prepareQuizData(): any {
    const formValue = this.quizForm.value;
    
    return {
      title: formValue.title.trim(),
      level: formValue.level,
      questions: formValue.questions.map((question: any) => ({
        text: question.text.trim(),
        points: Number(question.points),
        time: Number(question.time),
        technology: question.technology,
        options: question.options.map((option: any) => ({
          text: option.text.trim(),
          isCorrect: Boolean(option.isCorrect)
        }))
      }))
    };
  }

  /**
   * Mark all form fields as touched to show validation errors
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
   * Custom validator to ensure minimum number of options
   */
  private minOptionsValidator(min: number) {
    return (formArray: FormArray) => {
      return formArray.length >= min ? null : { minOptions: { requiredLength: min, actualLength: formArray.length } };
    };
  }

  /**
   * Calculate total quiz time
   */
  calculateTotalTime(): number {
    return this.questions.controls.reduce((total, question) => {
      const time = question.get('time')?.value || 0;
      return total + Number(time);
    }, 0);
  }

  /**
   * Close the modal without saving
   */
  closeModal(): void {
    if (this.quizForm.dirty) {
      if (confirm('Vous avez des modifications non sauvegardées. Voulez-vous vraiment fermer ?')) {
        this.activeModal.dismiss();
      }
    } else {
      this.activeModal.dismiss();
    }
  }

  /**
   * Load available technologies
   */
  loadTechnologies(): void {
    this.technologyService.getAll().subscribe(
      (data) => {
        this.technologies = data;
        console.log('Technologies loaded:', data);
      },
      (error) => {
        console.error('Erreur lors du chargement des technologies:', error);
        alert('Impossible de charger les technologies. Veuillez réessayer.');
      }
    );
  }
}