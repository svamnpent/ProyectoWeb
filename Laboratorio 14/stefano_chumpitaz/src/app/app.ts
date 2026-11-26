import { Component } from '@angular/core';
import { UserComponent } from './paginas/user/user.component';

@Component({
  selector: 'app-root',
  imports: [UserComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {}
