import { Component, OnInit } from '@angular/core';
import {Color} from 'ng2-charts/ng2-charts';
import { faTh, faCheck, faTrash, faAngleDown, faAngleUp } from '@fortawesome/free-solid-svg-icons';
import { ToastrService } from 'ngx-toastr';


@Component({
  selector: 'app-analytics',
  templateUrl: './analytics.component.html',
})
export class AnalyticsComponent implements OnInit {
  ngOnInit(): void {
    throw new Error('Method not implemented.');
  }

  faTh = faTh;
  faCheck = faCheck;
  faTrash = faTrash;
  faAngleDown = faAngleDown;
  faAngleUp = faAngleUp;

  heading = 'RGI Tunis';
  subheading = 'We re Hiring! Come And Join Us To Build Your Dream.';
  icon = 'pe-7s-plane icon-gradient bg-tempting-azure';

// Index pour le suivi des questions
currentIndex: number = 0;

// Questions du quiz
questions = [
  {
    text: 'Quelle est la différence entre une classe abstraite et une interface en Java ?',
    type: 'radio',
    options: [
      { label: 'Une classe abstraite peut avoir des méthodes implémentées.', value: 'option1' },
      { label: 'Une interface peut avoir des attributs privés.', value: 'option2' }
    ],
    answer: null
  },
  {
    text: 'Quelles annotations sont utilisées pour Spring ?',
    type: 'checkbox',
    options: [
      { label: '@Service', value: '@Service' },
      { label: '@Controller', value: '@Controller' },
      { label: '@Entity', value: '@Entity' }
    ],
    answer: []
  },
  {
    text: 'Expliquez le concept d\'API REST.',
    type: 'text',
    answer: ''
  }
];

// Statut des questions
answeredQuestions: boolean[] = Array(this.questions.length).fill(false);

// Met à jour une réponse radio ou texte
updateAnswer(index: number, value: any): void {
  this.questions[index].answer = value;
  this.answeredQuestions[index] = true;
}

// Met à jour une réponse checkbox
updateCheckboxAnswer(index: number, value: any, event: any): void {
  const answers = this.questions[index].answer as any[];
  if (event.target.checked) {
    answers.push(value);
  } else {
    const idx = answers.indexOf(value);
    if (idx !== -1) answers.splice(idx, 1);
  }
  this.answeredQuestions[index] = true;
}

// Désactive les champs pour les questions répondue
isQuestionAnswered(index: number): boolean {
  return this.answeredQuestions[index];
}

// Soumission du quiz
submitQuiz(): void {
  console.log('Réponses du quiz :', this.questions);
  alert('Quiz soumis avec succès !');
}
}