import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PostService } from 'src/app/core/services/post.service';

@Component({
  selector: 'app-detaille-poste',
  templateUrl: './detaille-poste.component.html',
  styleUrls: ['./detaille-poste.component.sass']
})
export class DetaillePosteComponent implements OnInit {

  post : any;
  postId: string
  constructor(private route: ActivatedRoute, private postService: PostService) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.postId = params.id;
  });
  this.loadData();
  }

  loadData(): void {
    this.postService.getPostById(this.postId).subscribe(result => {this.post = result})
  }

}
