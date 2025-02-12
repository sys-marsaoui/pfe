import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PostService } from 'src/app/core/services/post.service';
import { TokenDecoder } from 'src/app/shared/utils/jwt.util';

@Component({
  selector: 'app-detaille-poste',
  templateUrl: './detaille-poste.component.html',
  styleUrls: ['./detaille-poste.component.sass']
})
export class DetaillePosteComponent implements OnInit {
  post: any; // Contient les données du poste
  postId: string; // ID du poste
  user:any
  userAnswer:any;

  constructor(private route: ActivatedRoute, private postService: PostService, private tokenDecoder: TokenDecoder) {}

  ngOnInit(): void {
    // Récupérer l'ID depuis l'URL
    this.route.params.subscribe((params) => {
      this.postId = params['id'];
      this.loadData();
      this.user = this.tokenDecoder.getUser();
      if(this.user.role === "CONDIDATE"){
        this.loadPostStat();

      }
    });
  }

  loadPostStat(){
    this.postService.getStatByUser(this.postId, this.user?.id).subscribe(res=>{
      this.userAnswer =res;
    })
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
