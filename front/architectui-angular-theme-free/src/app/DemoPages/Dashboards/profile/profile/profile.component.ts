// src/app/profile/profile.component.ts
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from 'src/app/core/services/auth-service';
import { environment } from 'src/environments/environment';
import { FormBuilder, FormGroup, Validators, FormArray } from '@angular/forms';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
  user: any = {};
  cvData: any = null;
  selectedFile: File | null = null;
  uploadMessage: string = '';
  isLoading: boolean = false;
  isFetchingCv: boolean = false;
  isEditMode: boolean = false;
  cvForm: FormGroup;
  isSidebarCollapsed = false;
  postDetails: any = null;
  isQuizAccepted: boolean = false;
  isGeneratingCv = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private http: HttpClient,
    private fb: FormBuilder
  ) {
    this.cvForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: [''],
      address: [''],
      linkedin: [''],
      github: [''],
      experiences: this.fb.array([]),
      projects: this.fb.array([]),
      skills: this.fb.array([]),
      education: this.fb.array([])
    });
  }

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      console.error('User not logged in, redirecting to login');
      this.router.navigate(['/login']);
      return;
    }
    this.user = this.authService.getUserDetails() || {};
    this.fetchCvData();
    if (this.user.postId) {
      this.fetchPostDetails(this.user.postId);
      this.checkQuizAcceptance(this.user.postId);
    } else {
      console.warn('No postId found for user');
    }
  }

  private checkQuizAcceptance(postId: string): void {
    const token = this.authService.getToken();
    if (!token) {
      console.error('No token found for quiz acceptance check');
      this.uploadMessage = 'Session expired. Please log in again.';
      this.router.navigate(['/login']);
      return;
    }
    this.http.get<boolean>(`${environment.apiUrl}/api/quizzes/acceptance?postId=${postId}`, {
      headers: new HttpHeaders({ 'Authorization': `Bearer ${token}` }),
      withCredentials: true
    }).subscribe({
      next: (response) => {
        this.isQuizAccepted = response;
      },
      error: (error) => {
        this.isQuizAccepted = false;
        this.handleHttpError(error, 'check quiz acceptance');
      }
    });
  }

startQuiz(): void {
  if (this.postDetails?.quizId && this.postDetails?.id) {
    const quizId = this.postDetails.quizId;
    const postId = this.postDetails.id;

    // Navigate with postId as query param
    this.router.navigate(['/quiz', quizId], {
      queryParams: { postId }
    });
  } else {
    this.uploadMessage = 'Quiz not available.';
  }
}

  private fetchPostDetails(postId: string): void {
    const token = this.authService.getToken();
    if (!token) {
      console.error('No token found for fetching post details');
      return;
    }
    this.http.get<any>(`${environment.apiUrl}/api/posts/${postId}`, {
      headers: new HttpHeaders({ 'Authorization': `Bearer ${token}` }),
      withCredentials: true
    }).subscribe({
      next: (response) => {
        this.postDetails = response;
      },
      error: (error) => {
        this.handleHttpError(error, 'fetch post details');
      }
    });
  }

  toggleSidebar(): void {
    this.isSidebarCollapsed = !this.isSidebarCollapsed;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;

    const file = input.files[0];
    const allowedTypes = ['application/pdf', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
    const maxSize = 5 * 1024 * 1024; // 5MB

    if (!allowedTypes.includes(file.type)) {
      this.uploadMessage = 'Please select a PDF or DOCX file.';
      this.selectedFile = null;
      input.value = '';
      console.warn('Invalid file type selected:', file.type);
      return;
    }
    if (file.size > maxSize) {
      this.uploadMessage = 'File size must be less than 5MB.';
      this.selectedFile = null;
      input.value = '';
      console.warn('File size too large:', file.size);
      return;
    }
    this.selectedFile = file;
    this.uploadMessage = '';
  }

  uploadCv(): void {
    if (!this.selectedFile) {
      this.uploadMessage = 'Please select a file first.';
      console.warn('No file selected for upload');
      return;
    }

    this.isLoading = true;
    const formData = new FormData();
    formData.append('file', this.selectedFile);

    const token = this.authService.getToken();
    if (!token) {
      this.uploadMessage = 'Session expired. Please log in again.';
      this.isLoading = false;
      this.router.navigate(['/login']);
      console.error('No token found for CV upload');
      return;
    }

    this.http.post<any>(`${environment.apiUrl}/api/profile/upload-cv`, formData, {
      headers: new HttpHeaders({ 'Authorization': `Bearer ${token}` }),
      withCredentials: true
    }).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.uploadMessage = 'CV uploaded successfully!';
        this.selectedFile = null;
        const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
        if (fileInput) fileInput.value = '';
        this.fetchCvData();
      },
      error: (error) => {
        this.isLoading = false;
        this.handleHttpError(error, 'upload CV');
        this.fetchCvData();
      }
    });
  }

  fetchCvData(): void {
    const token = this.authService.getToken();
    if (!token) {
      this.uploadMessage = 'Session expired. Please log in again.';
      this.router.navigate(['/login']);
      console.error('No token found for fetching CV data');
      return;
    }

    this.isFetchingCv = true;
    // Use new endpoint /api/profile/cv-data (no userId)
    this.http.get<any>(`${environment.apiUrl}/api/profile/cv-data`, {
      headers: new HttpHeaders({ 'Authorization': `Bearer ${token}` }),
      withCredentials: true
    }).subscribe({
      next: (response) => {
        this.isFetchingCv = false;
        this.parseAndSetCvData(response);
        this.uploadMessage = '';
      },
      error: (error) => {
        this.isFetchingCv = false;
        this.handleHttpError(error, 'fetch CV data');
      }
    });
  }

  private parseAndSetCvData(data: any): void {
    if (!data) {
      this.cvData = null;
      return;
    }
    this.cvData = {
      name: data.name || 'Unknown',
      email: data.email || '',
      phone: data.phone || '',
      address: data.address || '',
      linkedin: data.linkedin || '',
      github: data.github || '',
      photoPath: data.photoPath || '',
      cvPath: data.cvPath || '',
      experiences: this.safeParseJson(data.experiences) || [],
      projects: this.safeParseJson(data.projects) || [],
      skills: this.safeParseJson(data.skills) || [],
      education: this.safeParseJson(data.education) || [],
      isValidated: data.isValidated || false
    };
    this.initForm();
  }

  private safeParseJson(jsonString: any): any {
    if (!jsonString) return [];
    if (typeof jsonString === 'object') return jsonString;
    try {
      return JSON.parse(jsonString);
    } catch (e) {
      console.error('Error parsing JSON:', e);
      return [];
    }
  }

  private initForm(): void {
    this.cvForm.patchValue({
      name: this.cvData.name,
      email: this.cvData.email,
      phone: this.cvData.phone,
      address: this.cvData.address,
      linkedin: this.cvData.linkedin,
      github: this.cvData.github
    });

    const experiencesArray = this.cvForm.get('experiences') as FormArray;
    experiencesArray.clear();
    this.cvData.experiences.forEach((exp: any) => {
      experiencesArray.push(this.fb.group({
        position: [exp.position],
        company: [exp.company],
        period: [exp.period],
        description: [exp.description]
      }));
    });

    const projectsArray = this.cvForm.get('projects') as FormArray;
    projectsArray.clear();
    this.cvData.projects.forEach((proj: any) => {
      projectsArray.push(this.fb.group({
        title: [proj.title],
        period: [proj.period],
        description: [proj.description]
      }));
    });

    const skillsArray = this.cvForm.get('skills') as FormArray;
    skillsArray.clear();
    this.cvData.skills.forEach((skill: string) => {
      skillsArray.push(this.fb.control(skill));
    });

    const educationArray = this.cvForm.get('education') as FormArray;
    educationArray.clear();
    this.cvData.education.forEach((edu: any) => {
      educationArray.push(this.fb.group({
        degree: [edu.degree],
        period: [edu.period]
      }));
    });
  }

  beginCvCreation(): void {
    this.cvData = {
      name: `${this.user.firstName || ''} ${this.user.lastName || ''}`.trim(),
      email: this.user.email || '',
      phone: this.user.tel || '',
      address: this.user.address || '',
      linkedin: '', github: '', photoPath: '', cvPath: '',
      experiences: [], projects: [], skills: [], education: [], isValidated: false
    };
    this.initForm();
    this.isEditMode = true;
  }

  generateCvWithAi(): void {
    const token = this.authService.getToken();
    if (!token) {
      this.router.navigate(['/login']);
      return;
    }
    this.isGeneratingCv = true;
    const postId = this.postDetails?.id || this.user.postId;
    const options = {
      headers: new HttpHeaders({ 'Authorization': `Bearer ${token}` }),
      withCredentials: true
    };
    const url = `${environment.apiUrl}/api/profile/generate-cv` +
      (postId ? `?postId=${encodeURIComponent(postId)}` : '');
    this.http.post<any>(url, {}, options).subscribe({
      next: (response) => {
        this.isGeneratingCv = false;
        this.parseAndSetCvData(response);
        this.isEditMode = true;
        this.uploadMessage = 'CV généré avec Gemini. Vérifiez et corrigez chaque information avant de le valider.';
      },
      error: (error) => {
        this.isGeneratingCv = false;
        this.uploadMessage = error.error?.detail || error.error?.message ||
          'Génération IA indisponible. Vérifiez GEMINI_API_KEY et réessayez.';
      }
    });
  }

  toggleEditMode(): void {
    this.isEditMode = !this.isEditMode;
    console.log('Edit mode toggled:', this.isEditMode);
  }

  saveChanges(): void {
    if (this.cvForm.invalid) {
      this.uploadMessage = 'Please fill all required fields.';
      console.warn('CV form invalid:', this.cvForm.errors);
      return;
    }

    const cvDataDto = {
      name: this.cvForm.value.name,
      email: this.cvForm.value.email,
      phone: this.cvForm.value.phone,
      address: this.cvForm.value.address,
      linkedin: this.cvForm.value.linkedin,
      github: this.cvForm.value.github,
      experiences: JSON.stringify(this.cvForm.value.experiences),
      projects: JSON.stringify(this.cvForm.value.projects),
      skills: JSON.stringify(this.cvForm.value.skills),
      education: JSON.stringify(this.cvForm.value.education)
    };

    const token = this.authService.getToken();
    if (!token) {
      this.uploadMessage = 'Session expired. Please log in again.';
      this.router.navigate(['/login']);
      console.error('No token found for updating CV');
      return;
    }

    this.http.put<any>(`${environment.apiUrl}/api/profile/update-cv`, cvDataDto, {
      headers: new HttpHeaders({ 'Authorization': `Bearer ${token}` }),
      withCredentials: true
    }).subscribe({
      next: (response) => {
        this.uploadMessage = 'CV updated successfully!';
        this.isEditMode = false;
        this.fetchCvData();
      },
      error: (error) => {
        this.handleHttpError(error, 'update CV');
      }
    });
  }

  validateCv(): void {
    const token = this.authService.getToken();
    if (!token) {
      this.uploadMessage = 'Session expired. Please log in again.';
      this.router.navigate(['/login']);
      console.error('No token found for validating CV');
      return;
    }

    console.log('Validating CV');
    this.http.post<any>(`${environment.apiUrl}/api/profile/validate-cv`, {}, {
      headers: new HttpHeaders({ 'Authorization': `Bearer ${token}` }),
      withCredentials: true
    }).subscribe({
      next: (response) => {
        this.uploadMessage = 'CV validated successfully!';
        this.fetchCvData();
      },
      error: (error) => {
        this.handleHttpError(error, 'validate CV');
        this.fetchCvData();
      }
    });
  }

  deleteCv(): void {
    if (!confirm('Are you sure you want to delete your CV?')) {
      console.log('CV deletion cancelled');
      return;
    }

    const token = this.authService.getToken();
    if (!token) {
      this.uploadMessage = 'Session expired. Please log in again.';
      this.router.navigate(['/login']);
      console.error('No token found for deleting CV');
      return;
    }

    console.log('Deleting CV');
    this.http.delete<any>(`${environment.apiUrl}/api/profile/delete-cv`, {
      headers: new HttpHeaders({ 'Authorization': `Bearer ${token}` }),
      withCredentials: true
    }).subscribe({
      next: (response) => {
        this.uploadMessage = 'CV deleted successfully!';
        this.cvData = null;
      },
      error: (error) => {
        this.handleHttpError(error, 'delete CV');
        this.fetchCvData();
      }
    });
  }

  get experiences(): FormArray {
    return this.cvForm.get('experiences') as FormArray;
  }

  addExperience(): void {
    this.experiences.push(this.fb.group({
      position: [''],
      company: [''],
      period: [''],
      description: ['']
    }));
    console.log('Added new experience field');
  }

  removeExperience(index: number): void {
    this.experiences.removeAt(index);
    console.log('Removed experience at index:', index);
  }

  get projects(): FormArray {
    return this.cvForm.get('projects') as FormArray;
  }

  addProject(): void {
    this.projects.push(this.fb.group({
      title: [''],
      period: [''],
      description: ['']
    }));
    console.log('Added new project field');
  }

  removeProject(index: number): void {
    this.projects.removeAt(index);
    console.log('Removed project at index:', index);
  }

  get skills(): FormArray {
    return this.cvForm.get('skills') as FormArray;
  }

  addSkill(): void {
    this.skills.push(this.fb.control(''));
    console.log('Added new skill field');
  }

  removeSkill(index: number): void {
    this.skills.removeAt(index);
    console.log('Removed skill at index:', index);
  }

  get education(): FormArray {
    return this.cvForm.get('education') as FormArray;
  }

  addEducation(): void {
    this.education.push(this.fb.group({
      degree: [''],
      period: ['']
    }));
    console.log('Added new education field');
  }

  removeEducation(index: number): void {
    this.education.removeAt(index);
    console.log('Removed education at index:', index);
  }

  private handleHttpError(error: any, action: string): void {
    console.error(`Error during ${action}:`, error);
    if (error.status === 401) {
      this.uploadMessage = 'Session expired. Please log in again.';
      this.router.navigate(['/login']);
    } else if (error.status === 403) {
      this.uploadMessage = `Access denied for ${action}. Please ensure your account has the correct permissions (USER, ADMIN, or CANDIDATE role).`;
      console.warn(`403 Forbidden for ${action}: Check token roles and endpoint permissions`);
    } else if (error.status === 413) {
      this.uploadMessage = 'File too large. Please select a smaller file.';
    } else if (error.status === 404 && action === 'fetch CV data') {
      this.cvData = null;
      this.uploadMessage = 'No CV data found. Upload a CV to get started.';
    } else {
      this.uploadMessage = `Error during ${action}: ${error.error?.message || error.message || 'Unknown error'}`;
    }
  }
}
