import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-detaille-poste',
  templateUrl: './detaille-poste.component.html',
  styleUrls: ['./detaille-poste.component.sass']
})
export class DetaillePosteComponent implements OnInit {

  poste : any= {id:'123'};
  constructor() { }

  ngOnInit(): void {
  }

}
