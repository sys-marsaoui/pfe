import { Component, OnInit } from '@angular/core';
import { RegisterBoxedServices } from './register-boxed.service';
import {  Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-register-boxed',
  templateUrl: './register-boxed.component.html',
  styles: []
})
export class RegisterBoxedComponent implements OnInit {
  userCredentials: any = {};
  post: string;
  constructor(private registerBoxedServices: RegisterBoxedServices, private route: ActivatedRoute,
     private router: Router ) { }

  ngOnInit() {
     this.route.queryParams.subscribe(params => {
              this.post = params.post;
          });
  }

   createUser() {
this.registerBoxedServices.postData(this.userCredentials, this.post).subscribe( response => {
 this.router.navigate(['/login' ]);

}, error => {
  console.error('Error posting data:',);
  // Handle error
});
}

}
