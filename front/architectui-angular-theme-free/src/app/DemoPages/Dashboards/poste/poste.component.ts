import { Component, OnInit } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { PostService } from 'src/app/core/services/post.service';
import { NewPosteComponent } from 'src/app/feature/poste/new-poste/new-poste.component';

@Component({
  selector: 'app-poste',
  templateUrl: './poste.component.html',
  styleUrls: ['./poste.component.sass']
})
export class PosteComponent implements OnInit {
  posts: any[] = [];

  constructor(private postService: PostService, private modalService: NgbModal) {}

  ngOnInit(): void {
    this.loadPosts();
  }

  loadPosts(): void {
    this.postService.getAllPosts().subscribe(
      (data) => {
        this.posts = data;
      },
      (error) => {
        console.error('Error loading posts:', error);
      }
    );
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

  openCreatePostModal(): void {
    const modalRef = this.modalService.open(NewPosteComponent, { size: 'lg', backdrop: 'static' });
    modalRef.result.then(
      (result) => {
        if (result) {
          this.loadPosts();
        }
      },
      () => {
        console.log('Modal fermé.');
      }
    );
  }
}