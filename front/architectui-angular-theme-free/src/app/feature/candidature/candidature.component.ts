import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-candidature',
  templateUrl: './candidature.component.html',
  styleUrls: ['./candidature.component.sass']
})
export class CandidatureComponent implements OnInit {
  heading = 'RGI Tunis';
  subheading = 'We re Hiring! Come And Join Us To Build Your Dream.';
  icon = 'pe-7s-graph text-success';

  constructor() { }

  ngOnInit(): void {
  }

}
