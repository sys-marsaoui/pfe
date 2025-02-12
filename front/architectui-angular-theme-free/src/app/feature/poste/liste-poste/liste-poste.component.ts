import { Component, OnInit } from '@angular/core';
import { PostService } from 'src/app/core/services/post.service';
import { NewPosteComponent } from '../new-poste/new-poste.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Router } from '@angular/router';
import { FormGroup } from '@angular/forms';

@Component({
  selector: 'app-liste-poste',
  templateUrl: './liste-poste.component.html',
  styleUrls: ['./liste-poste.component.sass']
})
export class ListePosteComponent implements OnInit {
  heading = 'RGI Tunis';
  subheading = 'We re Hiring! Come And Join Us To Build Your Dream.';
  icon = 'pe-7s-plane icon-gradient bg-tempting-azure';
  posts: any[] = [];
    postForm: FormGroup;
  
  post: any;

  constructor(private postService: PostService, private modalService: NgbModal, private router: Router) {}

  ngOnInit(): void {
    this.loadPosts();
    if (this.post) {
      // Si un poste est passé, pré-remplir le formulaire pour la mise à jour
      this.postForm.patchValue({
        title: this.post.title,
        description: this.post.description,
        contractType: this.post.contractType,
        quizId: this.post.quizId,
      });
    }
  }

  deletePost(id: string): void {
    if (confirm('Voulez-vous vraiment supprimer ce poste ?')) {
      this.postService.deletePost(id).subscribe(
        () => {
          this.posts = this.posts.filter((post) => post.id !== id);
        },
        (error) => {
          console.error('Erreur lors de la suppression:', error);
        }
      );
    }
  }

    // Charger les postes
    loadPosts(): void {
      this.postService.getAllPosts().subscribe(
        (data) => {
          this.posts = data;
        },
        (error) => {
          console.error('Erreur lors du chargement des postes:', error);
        }
      );
    }
  
// Ouvrir le modal pour créer un poste
openCreatePostModal(): void {
  const modalRef = this.modalService.open(NewPosteComponent, { size: 'lg', backdrop: 'static' });
  modalRef.result.then(
    (result) => {
      if (result) {
        this.loadPosts(); // Recharger les postes après la création
      }
    },
    () => {
      console.log('Modal fermé.');
    }
  );
}


openDetail(idPost: string): void {
  this.router.navigateByUrl('/detaille-poste/'+ idPost)
}
openUpdatePostModal(post: any): void {
  const modalRef = this.modalService.open(NewPosteComponent, { size: 'lg', backdrop: 'static' });
  modalRef.componentInstance.post = post; // Passer les données du poste au composant modal
  modalRef.result.then(
    (result) => {
      if (result) {
        this.loadPosts(); // Recharger les postes après la mise à jour
      }
    },
    () => {
      console.log('Modal fermé.');
    }
  );
}

}