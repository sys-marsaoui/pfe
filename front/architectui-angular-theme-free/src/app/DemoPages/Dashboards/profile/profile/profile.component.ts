import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';


@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.sass']
})
export class ProfileComponent implements OnInit {


  ngOnInit(): void {
  }
  profileForm: FormGroup;
  files: { [key: string]: File } = {};

  constructor(private fb: FormBuilder) {
    this.profileForm = this.fb.group({
      username: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', Validators.required],
      linkedin: ['', Validators.required],
    });
  }

  // Handle file input change
  onFileChange(event: any, field: string) {
    if (event.target.files.length > 0) {
      const file = event.target.files[0];
      this.files[field] = file;
    }
  }

  // Submit form
  onSubmit() {
    if (this.profileForm.valid) {
      const formData = new FormData();

      // Append form values
      Object.keys(this.profileForm.controls).forEach(key => {
        formData.append(key, this.profileForm.get(key)?.value);
      });

      // Append files
      Object.keys(this.files).forEach(key => {
        formData.append(key, this.files[key]);
      });

      // Send formData to your backend API
      console.log('Form Data:', formData);
      // Example: this.http.post('your-api-endpoint', formData).subscribe(...);
    }
  }

}
