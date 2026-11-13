import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PostService } from 'src/app/core/services/post.service';
import { TokenDecoder } from 'src/app/shared/utils/jwt.util';
import { CandidatureService } from 'src/app/core/services/candidature.service';

@Component({
  selector: 'app-detaille-poste',
  templateUrl: './detaille-poste.component.html',
  styleUrls: ['./detaille-poste.component.scss']
})
export class DetaillePosteComponent implements OnInit {
  post: any; // Contient les données du poste
  postId: string; // ID du poste
  user: any = {};
  userAnswer:any;
  applicationMessage = '';
  isApplying = false;

  constructor(private route: ActivatedRoute, private postService: PostService, private tokenDecoder: TokenDecoder,
              private candidatureService: CandidatureService) {}

  ngOnInit(): void {
    // Récupérer l'ID depuis l'URL
    this.route.params.subscribe((params) => {
      this.postId = params['id'];
      this.loadData();
      this.user = this.tokenDecoder.getUser();
      if (this.user.role === 'CANDIDATE') {
        this.loadPostStat();

      }
    });
  }

  loadPostStat(){
    this.postService.getStatByUser(this.postId, this.user?.id).subscribe(res=>{
      this.userAnswer =res;
    })
  }

  get hasPassedQuiz(): boolean {
    if (!this.userAnswer || !this.userAnswer.scoreDescription) return false;
    const scores = String(this.userAnswer.scoreDescription).split('/').map(value => Number(value.trim()));
    return scores.length === 2 && scores[1] > 0 && scores[0] * 100 / scores[1] >= 70;
  }

  applyWithCv(): void {
    if (!this.post?.id || this.isApplying) return;
    this.isApplying = true;
    this.applicationMessage = '';
    this.candidatureService.applyWithCv(this.post.id).subscribe({
      next: () => {
        this.isApplying = false;
        this.user.postId = this.post.id;
        this.applicationMessage = 'Votre candidature avec le CV validé a été enregistrée.';
      },
      error: (error) => {
        this.isApplying = false;
        this.applicationMessage = error.error?.message || error.error?.detail || 'Validez votre CV depuis votre profil avant de postuler.';
      }
    });
  }

  // Charger les données du poste depuis le service
  loadData(): void {
    this.postService.getPostById(this.postId).subscribe(
      (result) => {
        this.post = result; // Assigner les données au poste
      },
      (error) => {
        console.error('Erreur lors du chargement du poste:', error);
      }
    );
  }
}
