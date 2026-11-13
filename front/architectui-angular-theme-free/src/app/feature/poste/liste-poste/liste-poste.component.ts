import { Component, OnInit } from '@angular/core';
import { PostService, AtsMatchResult } from 'src/app/core/services/post.service';
import { NewPosteComponent } from '../new-poste/new-poste.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ActivatedRoute, Router } from '@angular/router';
import { TokenDecoder } from 'src/app/shared/utils/jwt.util';
import { LinkedInService } from 'src/app/core/services/linkedin.service';

@Component({
  selector: 'app-liste-poste',
  templateUrl: './liste-poste.component.html',
  styleUrls: ['./liste-poste.component.scss']
})
export class ListePosteComponent implements OnInit {
  heading = 'RGI Tunis';
  subheading = 'We\'re Hiring! Come And Join Us To Build Your Dream.';
  icon = 'pe-7s-plane icon-gradient bg-tempting-azure';
  posts: any[] = [];
  isAdmin = false;
  isSharing: Record<string, boolean> = {};
  linkedinMessage = '';

  constructor(
    private postService: PostService,
    private modalService: NgbModal,
    private router: Router,
    private tokenDecoder: TokenDecoder,
    private linkedInService: LinkedInService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.isAdmin = this.tokenDecoder.getUser().role === 'ADMIN';
    this.route.queryParamMap.subscribe((params) => {
      const result = params.get('linkedin');
      this.linkedinMessage = result === 'published'
        ? 'L’offre a été publiée sur LinkedIn.'
        : result === 'error' ? 'La publication LinkedIn a échoué ou a été annulée.' : '';
    });
    this.loadPosts();
  }

  shareOnLinkedIn(postId: string): void {
    if (this.isSharing[postId]) return;
    this.isSharing[postId] = true;
    this.linkedInService.getOfferAuthorizationUrl(postId).subscribe({
      next: ({ authorizationUrl }) => window.location.assign(authorizationUrl),
      error: (error) => {
        this.isSharing[postId] = false;
        alert(error.error?.detail || error.error?.message ||
          'LinkedIn n’est pas configuré. Vérifiez la configuration du backend.');
      }
    });
  }

  deletePost(id: string): void {
    if (confirm('Are you sure you want to delete this post?')) {
      this.postService.deletePost(id).subscribe(
        () => {
          this.posts = this.posts.filter((post) => post.id !== id);
        },
        (error) => {
          console.error('Error during deletion:', error);
        }
      );
    }
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

  openCreatePostModal(): void {
    const modalRef = this.modalService.open(NewPosteComponent, { size: 'lg', backdrop: 'static' });
    modalRef.result.then(
      (result) => {
        if (result) {
          this.loadPosts(); // Reload posts after creation
        }
      },
      () => {
        console.log('Modal closed.');
      }
    );
  }

  openDetail(idPost: string): void {
    this.router.navigateByUrl('/detaille-poste/' + idPost);
  }

  registerForPost(postId: string): void {
    this.router.navigate(['/register'], { queryParams: { post: postId } });
  }

  openUpdatePostModal(post: any): void {
    const modalRef = this.modalService.open(NewPosteComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.post = post; // Pass post data to the modal component
    modalRef.result.then(
      (result) => {
        if (result) {
          this.loadPosts(); // Reload posts after update
        }
      },
      () => {
        console.log('Modal closed.');
      }
    );
  }

  runAts(postId: string): void {
    this.postService.compareCvsToPostByPostId(postId).subscribe(
      (results: AtsMatchResult[]) => {
        if (results.length === 0) {
          alert('No ATS results found for this post.');
          return;
        }
        const resultMessage = results
          .map((result) => `Candidate: ${result.candidateName}, Match Score: ${result.matchScore.toFixed(2)}%`)
          .join('\n');
        alert(`ATS Results for Post ID ${postId}:\n\n${resultMessage}`);
      },
      (error) => {
        console.error('Error running ATS for post:', postId, error);
        alert('Failed to run ATS comparison. Please try again later.');
      }
    );
  }
}
