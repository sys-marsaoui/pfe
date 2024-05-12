import { Component, OnInit } from '@angular/core';
import { RegisterBoxedServices } from './register-boxed.service';

@Component({
  selector: 'app-register-boxed',
  templateUrl: './register-boxed.component.html',
  styles: []
})
export class RegisterBoxedComponent implements OnInit {
  userCredentials: any = {};
  constructor(private registerBoxedServices: RegisterBoxedServices) { }

  ngOnInit() {
    this.createUser();
  }
   createUser() {
this.registerBoxedServices.postData(this.userCredentials).subscribe(async response => {
  console.log('Post successful:', await response.json());
  // Reset form or handle success
}, error => {
  console.error('Error posting data:', error);
  // Handle error
});
}

}
