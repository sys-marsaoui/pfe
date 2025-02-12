import { Component, OnInit, Input } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { PostService } from 'src/app/core/services/post.service';

@Component({
  selector: 'app-new-poste',
  templateUrl: './new-poste.component.html',
  styleUrls: ['./new-poste.component.sass']
})
export class NewPosteComponent implements OnInit {
  @Input() post: any; // Données du poste pour la modification (null si création)
  postForm: FormGroup;
  quizzes: any[] = []; // Liste des quiz pour le menu déroulant

  constructor(
    private fb: FormBuilder,
    private postService: PostService,
    public activeModal: NgbActiveModal
  ) {
    this.postForm = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      contractType: ['', Validators.required],
      nbrOpenPost: ['1', Validators.required],
      quizId: [''], // Quiz associé
      responsibilities: this.fb.array([]) // Liste dynamique des responsabilités
    });
  }

  ngOnInit(): void {
    this.loadQuizzes();

    // Si un poste est passé pour modification, pré-remplir le formulaire
    if (this.post) {
      this.postForm.patchValue({
        title: this.post.title,
        description: this.post.description,
        contractType: this.post.contractType,
        quizId: this.post.quizId
      });

      // Ajouter les responsabilités existantes au formulaire
      this.post.responsabilites.forEach((res: string) => {
        this.addResponsibility(res);
      });
    }
  }

  // Charger la liste des quiz depuis le backend
  loadQuizzes(): void {
    this.postService.getAllPosts().subscribe(
      (data) => {
        this.quizzes = data;
      },
      (error) => {
        console.error('Erreur lors du chargement des quiz:', error);
      }
    );
  }

  // Getter pour accéder à la liste dynamique des responsabilités
  get responsibilities(): FormArray {
    return this.postForm.get('responsibilities') as FormArray;
  }

  // Ajouter une responsabilité
  addResponsibility(value: string = ''): void {
    const responsibilityGroup = this.fb.group({
      responsibility: [value, Validators.required]
    });
    this.responsibilities.push(responsibilityGroup);
  }

  // Supprimer une responsabilité
  removeResponsibility(index: number): void {
    this.responsibilities.removeAt(index);
  }

  // Enregistrer ou mettre à jour le poste
  savePost(): void {
    const postData = {
      ...this.postForm.value,
      responsabilites: this.postForm.value.responsibilities.map((r: any) => r.responsibility)
    };

    if (this.post) {
      // Mode mise à jour
      this.postService.updatePost(this.post.id, postData).subscribe(
        (response) => {
          console.log('Post mis à jour avec succès:', response);
          this.activeModal.close(response); // Fermer le modal avec succès
        },
        (error) => {
          console.error('Erreur lors de la mise à jour:', error);
        }
      );
    } else {
      // Mode création
      this.postService.createPost(postData).subscribe(
        (response) => {
          console.log('Post créé avec succès:', response);
          this.activeModal.close(response); // Fermer le modal avec succès
        },
        (error) => {
          console.error('Erreur lors de la création:', error);
        }
      );
    }
  }

  // Fermer le modal
  closeModal(): void {
    this.activeModal.dismiss();
  }
}
