import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';
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
  postId :string;
  constructor( private authService: AuthService,private router:Router,private toastService:ToastrService) { }
  
  ngOnInit() {
  }
  connect(){
this.authService.Auth(this.credentiels).subscribe((response)=>{
  if(response.code==200&&response.token!=null){
      this.authService.saveToken(response.token)
      this.toastService.success('Connected')
      this.postId = jwtDecode(localStorage.getItem('auth_token'))['user']?.postId;
      if(jwtDecode(localStorage.getItem('auth_token'))['user']?.role ==="CONDIDATE"){
        this.router.navigateByUrl('detaille-poste/'+ this.postId);
      }else {
        this.router.navigateByUrl('postes');
      }
  }else{
    this.toastService.error("Bad Credentiels")
  }
})    
  }
}

