import { Component } from '@angular/core';

@Component({
  selector: 'app-search-box',
  templateUrl: './search-box.component.html',
  styleUrls: ['./search-box.component.scss']
})
export class SearchBoxComponent {
  // État de la recherche (ouverte/fermée)
  isActive = false;

  constructor() {}

  // Appelé quand l'input perd le focus
  onBlur(): void {
    // Ferme la recherche après un petit délai
    // → permet de cliquer sur le bouton "X" ou la loupe
    setTimeout(() => {
      const input = document.querySelector('.search-input') as HTMLInputElement;
      if (input && input.value.trim() === '') {
        this.isActive = false;
      }
    }, 150);
  }
}