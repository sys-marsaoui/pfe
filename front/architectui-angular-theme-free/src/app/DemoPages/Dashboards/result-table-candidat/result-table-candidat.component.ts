import { Component, OnInit } from '@angular/core';
import {NgbModal, ModalDismissReasons} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-result-table-candidat',
  templateUrl: './result-table-candidat.component.html',
  styleUrls: ['./result-table-candidat.component.sass']
})
export class ResultTableCandidatComponent implements OnInit {


  candidats = [
    {
      nom: 'Houssem Marsaoui',
      resultat: '85%',
      statut: 'Accepté',
      dueDate: new Date(2024, 11, 20), // Example: December 20, 2024
      targetAchievement: 95
    },
    {
      nom: 'Marie Curie',
      resultat: '70%',
      statut: 'À traiter',
      dueDate: new Date(2024, 11, 25), // Example: December 25, 2024
      targetAchievement: 60
    },
    {
      nom: 'Albert Einstein',
      resultat: '90%',
      statut: 'Rejeté',
      dueDate: new Date(2024, 11, 15), // Example: December 15, 2024
      targetAchievement: 40
    },
    {
      nom: 'Sarra Newton',
      resultat: '95%',
      statut: 'Accepté',
      dueDate: new Date(2024, 11, 10), // Example: December 10, 2024
      targetAchievement: 80
    }
  ];
  heading = 'RGI Tunis';
  subheading = 'We re Hiring! Come And Join Us To Build Your Dream.';
  icon = 'pe-7s-phone icon-gradient bg-premium-dark';

  selectedCandidat: any = null;
  closeResult = '';

  constructor(private modalService: NgbModal) {}
  ngOnInit(): void {
    throw new Error('Method not implemented.');
  }

  // Ouvrir le modal avec gestion des résultats
  open(content: any, candidat: any) {
    this.selectedCandidat = candidat;
    this.modalService.open(content).result.then(
      (result) => {
        this.closeResult = `Closed with: ${result}`;
      },
      (reason) => {
        this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
      }
    );
  }

  // Ouvrir le modal centré
  openCentred(content: any, candidat: any) {
    this.selectedCandidat = candidat;
    this.modalService.open(content, { centered: true });
  }

  // Ouvrir le modal en taille réduite
  openSmall(content: any, candidat: any) {
    this.selectedCandidat = candidat;
    this.modalService.open(content, {
      size: 'sm'
    });
  }

  // Ouvrir le modal en grande taille
  openLarge(content: any, candidat: any) {
    this.selectedCandidat = candidat;
    this.modalService.open(content, {
      size: 'lg'
    });
  }

  // Gestion des raisons de fermeture du modal
  private getDismissReason(reason: any): string {
    if (reason === ModalDismissReasons.ESC) {
      return 'en appuyant sur Échap';
    } else if (reason === ModalDismissReasons.BACKDROP_CLICK) {
      return 'en cliquant sur l’arrière-plan';
    } else {
      return `avec : ${reason}`;
    }
  }

  // Confirmation de l'entretien
  confirmerEntretien() {
    if (this.selectedCandidat) {
      alert(
        `Entretien planifié pour ${this.selectedCandidat.nom} le ${this.selectedCandidat.entretienDate} à ${this.selectedCandidat.entretienHeure}`
      );
    }
    this.modalService.dismissAll();
  }
}
