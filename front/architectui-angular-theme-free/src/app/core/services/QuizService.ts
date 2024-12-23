import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class QuizService {
    private baseUrl = 'http://localhost:8080/api/quizzes'; // Backend API URL

    constructor(private http: HttpClient) {}
  
    // Get a quiz by its ID
    getQuizById(id: string): Observable<any> {
      return this.http.get(`${this.baseUrl}/${id}`);
    }
  
    // Submit quiz answers
    submitQuizAnswers(id: string, payload: any): Observable<any> {
      return this.http.post(`${this.baseUrl}/${id}/submit`, payload);
    }
  
    // Create a new quiz (optional if needed)
    createQuiz(quiz: any): Observable<any> {
      return this.http.post(this.baseUrl, quiz);
    }
  
    // Update an existing quiz (optional if needed)
    updateQuiz(id: string, quiz: any): Observable<any> {
      return this.http.put(`${this.baseUrl}/${id}`, quiz);
    }
  
    // Delete a quiz (optional if needed)
    deleteQuiz(id: string): Observable<any> {
      return this.http.delete(`${this.baseUrl}/${id}`);
    }



  // Get all quizzes
  getAllQuizzes(): Observable<any[]> {
    return this.http.get<any[]>(this.baseUrl);
  }





  }