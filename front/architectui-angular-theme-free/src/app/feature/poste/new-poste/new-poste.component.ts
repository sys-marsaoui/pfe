import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { PostService } from 'src/app/core/services/post.service';
import { QuizService } from 'src/app/core/services/QuizService';

@Component({
  selector: 'app-new-poste',
  templateUrl: './new-poste.component.html',
  styleUrls: ['./new-poste.component.sass']
})
export class NewPosteComponent implements OnInit {
  postForm: FormGroup;
  quizzes: any[] = [];

  constructor(
    private fb: FormBuilder,
    private postService: PostService,
    private quizService: QuizService,
    public activeModal: NgbActiveModal
  ) {
    this.postForm = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      contractType: ['', Validators.required],
      quizId: [null], // Quiz associé
    });
  }

  ngOnInit(): void {
    this.loadQuizzes();
  }

  // Charger la liste des quiz
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

  // Enregistrer le poste
  savePost(): void {
    if (this.postForm.valid) {
      this.postService.createPost(this.postForm.value).subscribe(
        (response) => {
          console.log('Post enregistré avec succès:', response);
          this.activeModal.close(response); // Fermer le modal après succès
        },
        (error) => {
          console.error('Erreur lors de l\'enregistrement du poste:', error);
        }
      );
    } else {
      console.error('Formulaire invalide');
    }
  }

  // Fermer le modal
  closeModal(): void {
    this.activeModal.dismiss();
  }
}
