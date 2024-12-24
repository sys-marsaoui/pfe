import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { QuizService } from 'src/app/core/services/QuizService';
import { CreateQuizComponent } from '../create-quiz/create-quiz.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-quiz',
  templateUrl: './quiz.component.html',
  styleUrls: ['./quiz.component.sass'],
  template: `<button class="btn btn-primary" (click)="openCreateQuizModal()">Créer un Quiz</button>`
})
export class QuizComponent implements OnInit {
  quizForm: FormGroup;
  quizzes: any[] = [];

  constructor(private fb: FormBuilder, private quizService: QuizService,private modalService: NgbModal) {
    this.quizForm = this.fb.group({
      title: ['', Validators.required],
      questions: this.fb.array([]),
    });

    // Add the first question by default
    this.addQuestion();
  }

  ngOnInit(): void {
    this.loadQuizzes();
  }

  // Getter for questions array
  get questions(): FormArray {
    return this.quizForm.get('questions') as FormArray;
  }

  // Add a new question
  addQuestion(): void {
    const questionGroup = this.fb.group({
      text: ['', Validators.required],
      points: [1, [Validators.required, Validators.min(1)]],
      options: this.fb.array([]),
    });
    this.questions.push(questionGroup);

    // Add two default options for each new question
    this.addOption(this.questions.length - 1);
    this.addOption(this.questions.length - 1);
  }

  // Remove a question
  removeQuestion(index: number): void {
    this.questions.removeAt(index);
  }

  // Getter for options array of a specific question
  getOptions(questionIndex: number): FormArray {
    return this.questions.at(questionIndex).get('options') as FormArray;
  }

  // Add an option to a specific question
  addOption(questionIndex: number): void {
    const options = this.getOptions(questionIndex);
    const optionGroup = this.fb.group({
      text: ['', Validators.required],
      isCorrect: [false],
    });
    options.push(optionGroup);
  }

  // Remove an option from a specific question
  removeOption(questionIndex: number, optionIndex: number): void {
    const options = this.getOptions(questionIndex);
    options.removeAt(optionIndex);
  }

  // Save the quiz
  saveQuiz(): void {
    if (this.quizForm.valid) {
      const quizData = this.quizForm.value;
      this.quizService.createQuiz(quizData).subscribe(
        (response) => {
          console.log('Quiz saved successfully:', response);
          this.loadQuizzes(); // Reload quizzes after saving
          this.quizForm.reset();
          alert('Quiz enregistré avec succès !');
        },
        (error) => {
          console.error('Error saving quiz:', error);
          alert('Erreur lors de l\'enregistrement du quiz.');
        }
      );
    } else {
      alert('Veuillez remplir tous les champs obligatoires.');
    }
  }

  // Load all quizzes
  loadQuizzes(): void {
    this.quizService.getAllQuizzes().subscribe(
      (data) => {
        this.quizzes = data;
      },
      (error) => {
        console.error('Error loading quizzes:', error);
      }
    );
  }
  openCreateQuizModal(): void {
    const modalRef = this.modalService.open(CreateQuizComponent, { size: 'lg', backdrop: 'static' });
    modalRef.result.then(
      (result) => {
        console.log('Quiz créé:', result);
      },
      (reason) => {
        console.log('Modal fermé:', reason);
      }
    );
  }
}