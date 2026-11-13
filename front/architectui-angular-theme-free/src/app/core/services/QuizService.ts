import { Injectable } from '@angular/core';
import { HttpClient,HttpParams  } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root',
})
export class QuizService {
    private baseUrl = environment.apiUrl + '/api/quizzes'; // Backend API URL

    constructor(private http: HttpClient) {}
  
    // Get a quiz by its ID
   
  
    // Submit quiz answers
    submitQuizAnswers(id: string, payload: any): Observable<any> {
      return this.http.post(`${this.baseUrl}/submit`, payload);
    }
  
    // Create a new quiz (optional if needed)
    createQuiz(quiz: any): Observable<any> {
      return this.http.post(this.baseUrl, quiz);
    }
  
    // Update an existing quiz (optional if needed)
   
  
    // Delete a quiz (optional if needed)
    deleteQuiz(id: string): Observable<any> {
      return this.http.delete(`${this.baseUrl}/${id}`);
    }



  // Get all quizzes
  getAllQuizzes(): Observable<any[]> {
    return this.http.get<any[]>(this.baseUrl);
  }
   getQuizById(id: string, postId?: string): Observable<any> {
    const params = postId ? new HttpParams().set('postId', postId) : undefined;
    return this.http.get<any>(`${this.baseUrl}/${id}`, { params });
  }
  updateQuiz(id: string, quizData: any, postId?: string): Observable<any> {
    let params = new HttpParams();
    if (postId) {
      params = params.set('postId', postId);
    }
    
    return this.http.put<any>(`${this.baseUrl}/${id}`, quizData, { params });
  }

 assignQuizToPost(payload: { quizId: string; postId: string }): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/assign`, payload);
  }
 filterQuizzes(filters?: {
    level?: string;
    technology?: string;
    minQuestions?: number;
    maxQuestions?: number;
  }): Observable<any[]> {
    let params = new HttpParams();
    
    if (filters) {
      if (filters.level) {
        params = params.set('level', filters.level);
      }
      if (filters.technology) {
        params = params.set('technology', filters.technology);
      }
      if (filters.minQuestions) {
        params = params.set('minQuestions', filters.minQuestions.toString());
      }
      if (filters.maxQuestions) {
        params = params.set('maxQuestions', filters.maxQuestions.toString());
      }
    }
    
    return this.http.get<any[]>(this.baseUrl, { params });
  }


  }
