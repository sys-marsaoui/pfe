import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import {ToastrService} from 'ngx-toastr'
import { AuthService } from 'src/app/core/services/auth-service';

@Component({
  selector: 'app-login-boxed',
  templateUrl: './login-boxed.component.html',
  styles: []
})


export class LoginBoxedComponent implements OnInit {
  credentiels={
    email:'',
    password:''
  }
  constructor( private authService: AuthService,private router:Router,private toastService:ToastrService) { }
  
  ngOnInit() {
  }
  connect(){
this.authService.Auth(this.credentiels).subscribe((response)=>{
  if(response.code==200&&response.token!=null){
      this.authService.saveToken(response.token)
      this.toastService.success('Connected')
      this.router.navigate(['/']);
  }else{
    this.toastService.error("Bad Credentiels")
  }
})    
  }
}

