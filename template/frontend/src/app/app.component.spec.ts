import { TestBed } from '@angular/core/testing';
import {HttpClient} from "@angular/common/http";
import { AppComponent } from './app.component';
import {RxStompService} from "./service/rx-stomp.service";

describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        AppComponent
      ],
      providers: [
        {provide: HttpClient, useValue: {}},
        {provide: RxStompService, useValue: {}}
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

});
