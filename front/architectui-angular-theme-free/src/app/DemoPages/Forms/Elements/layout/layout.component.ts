import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  styles: []
})
export class LayoutComponent implements OnInit {

  heading = 'RGI Tunis';
  subheading = 'We re Hiring! Come And Join Us To Build Your Dream.';
  icon = 'pe-7s-graph text-success';

  constructor() {
  }

  ngOnInit() {
  }

}
