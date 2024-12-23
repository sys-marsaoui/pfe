import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  success(message: string): void {
    alert(`✅ ${message}`); // Remplace par une bibliothèque de notifications si nécessaire
  }

  error(message: string): void {
    alert(`❌ ${message}`); // Remplace par une bibliothèque de notifications si nécessaire
  }
}
